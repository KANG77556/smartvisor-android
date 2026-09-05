use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_com_milsung_alldocviewer_hwp_NativeHwpBridge_nativeVersion(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    match env.new_string("hwp_renderer/0.1.0 rhwp/0.8.4") {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
