pub mod document_registry;
pub mod native_service;
pub mod render_request;

use crate::native_service::{NativeDocumentService, NativeServiceError};
use crate::render_request::RenderRequest;
use jni::objects::{JClass, JString};
use jni::sys::{jbyteArray, jdoubleArray, jint, jlong, jstring};
use jni::JNIEnv;
use std::fs;
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::ptr;
use std::sync::{Mutex, OnceLock};

const MAX_HWP_BYTES: u64 = 256 * 1024 * 1024;
static SERVICE: OnceLock<Mutex<NativeDocumentService>> = OnceLock::new();

fn service() -> &'static Mutex<NativeDocumentService> {
    SERVICE.get_or_init(|| Mutex::new(NativeDocumentService::new()))
}

fn service_error(error: NativeServiceError) -> String {
    match error {
        NativeServiceError::InvalidHandle(handle) => format!("INVALID_HANDLE|unknown handle: {handle}"),
        NativeServiceError::Parse(message) => format!("OPEN_FAILED|{message}"),
        NativeServiceError::Render(message) => format!("RENDER_FAILED|{message}"),
        NativeServiceError::Export(message) => format!("EXPORT_FAILED|{message}"),
    }
}

fn throw(env: &mut JNIEnv, encoded: impl AsRef<str>) {
    let _ = env.throw_new(
        "com/milsung/alldocviewer/hwp/HwpNativeException",
        encoded.as_ref(),
    );
}

fn with_service<T>(f: impl FnOnce(&mut NativeDocumentService) -> Result<T, String>) -> Result<T, String> {
    let mut guard = service().lock().map_err(|_| "NATIVE_ERROR|service lock poisoned".to_string())?;
    f(&mut guard)
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeVersion(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    match env.new_string("hwp_renderer/0.1.0 rhwp/0.8.4") {
        Ok(s) => s.into_raw(),
        Err(_) => ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeOpenDocument(
    mut env: JNIEnv,
    _class: JClass,
    path: JString,
) -> jlong {
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<jlong, String> {
        let path: String = env.get_string(&path)
            .map_err(|e| format!("INVALID_ARGUMENT|invalid path: {e}"))?
            .into();
        if path.is_empty() {
            return Err("INVALID_ARGUMENT|empty path".to_string());
        }
        let metadata = fs::metadata(&path).map_err(|e| format!("OPEN_FAILED|metadata: {e}"))?;
        if metadata.len() > MAX_HWP_BYTES {
            return Err(format!("OPEN_FAILED|file exceeds {} bytes", MAX_HWP_BYTES));
        }
        let bytes = fs::read(&path).map_err(|e| format!("OPEN_FAILED|read: {e}"))?;
        with_service(|service| service.open_bytes(&bytes).map(|h| h as jlong).map_err(service_error))
    }));
    match result {
        Ok(Ok(handle)) => handle,
        Ok(Err(message)) => { throw(&mut env, message); 0 }
        Err(_) => { throw(&mut env, "NATIVE_PANIC|native open panic"); 0 }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeCloseDocument(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) {
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<(), String> {
        if handle <= 0 { return Err("INVALID_HANDLE|handle must be positive".to_string()); }
        with_service(|service| service.close(handle as u64).map_err(service_error))
    }));
    match result {
        Ok(Ok(())) => {}
        Ok(Err(message)) => throw(&mut env, message),
        Err(_) => throw(&mut env, "NATIVE_PANIC|native close panic"),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeGetPageCount(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jint {
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<jint, String> {
        if handle <= 0 { return Err("INVALID_HANDLE|handle must be positive".to_string()); }
        with_service(|service| service.page_count(handle as u64).map(|v| v as jint).map_err(service_error))
    }));
    match result {
        Ok(Ok(count)) => count,
        Ok(Err(message)) => { throw(&mut env, message); 0 }
        Err(_) => { throw(&mut env, "NATIVE_PANIC|native page count panic"); 0 }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeGetPageInfo(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_index: jint,
) -> jdoubleArray {
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<(f64, f64), String> {
        if handle <= 0 { return Err("INVALID_HANDLE|handle must be positive".to_string()); }
        if page_index < 0 { return Err("INVALID_ARGUMENT|page index must be non-negative".to_string()); }
        with_service(|service| service.page_info(handle as u64, page_index as u32).map_err(service_error))
    }));
    match result {
        Ok(Ok((width, height))) => match env.new_double_array(2) {
            Ok(array) => {
                if env.set_double_array_region(&array, 0, &[width, height]).is_ok() {
                    array.into_raw()
                } else {
                    throw(&mut env, "JNI_ERROR|failed to fill page info");
                    ptr::null_mut()
                }
            }
            Err(e) => { throw(&mut env, format!("JNI_ERROR|page info array: {e}")); ptr::null_mut() }
        },
        Ok(Err(message)) => { throw(&mut env, message); ptr::null_mut() }
        Err(_) => { throw(&mut env, "NATIVE_PANIC|native page info panic"); ptr::null_mut() }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeRenderPagePng(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_index: jint,
    scale: f64,
    max_dimension: jint,
    max_pixels: jlong,
) -> jbyteArray {
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<Vec<u8>, String> {
        if handle <= 0 { return Err("INVALID_HANDLE|handle must be positive".to_string()); }
        if page_index < 0 || max_pixels <= 0 {
            return Err("INVALID_ARGUMENT|invalid page index or pixel limit".to_string());
        }
        let request = RenderRequest::new(page_index as u32, scale, max_dimension, max_pixels as u64)
            .map_err(|e| format!("INVALID_ARGUMENT|{e:?}"))?;
        with_service(|service| service.render_page_png(handle as u64, request).map_err(service_error))
    }));
    match result {
        Ok(Ok(bytes)) => match env.byte_array_from_slice(&bytes) {
            Ok(array) => array.into_raw(),
            Err(e) => { throw(&mut env, format!("JNI_ERROR|png array: {e}")); ptr::null_mut() }
        },
        Ok(Err(message)) => { throw(&mut env, message); ptr::null_mut() }
        Err(_) => { throw(&mut env, "NATIVE_PANIC|native render panic"); ptr::null_mut() }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeExportHwpx(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jbyteArray {
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<Vec<u8>, String> {
        if handle <= 0 { return Err("INVALID_HANDLE|handle must be positive".to_string()); }
        with_service(|service| service.export_hwpx(handle as u64).map_err(service_error))
    }));
    match result {
        Ok(Ok(bytes)) => match env.byte_array_from_slice(&bytes) {
            Ok(array) => array.into_raw(),
            Err(e) => { throw(&mut env, format!("JNI_ERROR|hwpx array: {e}")); ptr::null_mut() }
        },
        Ok(Err(message)) => { throw(&mut env, message); ptr::null_mut() }
        Err(_) => { throw(&mut env, "NATIVE_PANIC|native HWPX export panic"); ptr::null_mut() }
    }
}
