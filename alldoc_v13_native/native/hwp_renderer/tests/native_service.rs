use hwp_renderer::native_service::{NativeDocumentService, NativeServiceError};
use hwp_renderer::render_request::RenderRequest;
use rhwp::DocumentCore;

#[test]
fn malformed_document_is_rejected_without_allocating_handle() {
    let mut service = NativeDocumentService::new();
    let err = service.open_bytes(b"not-an-hwp").unwrap_err();
    assert!(matches!(err, NativeServiceError::Parse(_)));
    assert_eq!(service.open_document_count(), 0);
}

#[test]
fn invalid_handle_is_rejected() {
    let mut service = NativeDocumentService::new();
    assert!(matches!(service.page_count(42), Err(NativeServiceError::InvalidHandle(42))));
    assert!(matches!(service.page_info(42, 0), Err(NativeServiceError::InvalidHandle(42))));
    let request = RenderRequest::new(0, 1.0, 4096, 16_000_000).unwrap();
    assert!(matches!(service.render_page_png(42, request), Err(NativeServiceError::InvalidHandle(42))));
    assert!(matches!(service.export_hwpx(42), Err(NativeServiceError::InvalidHandle(42))));
    assert!(matches!(service.replace_all(42, "a", "b", true), Err(NativeServiceError::InvalidHandle(42))));
    assert!(matches!(service.render_page_svg(42, 0), Err(NativeServiceError::InvalidHandle(42))));
}

#[test]
fn replace_all_mutates_document_and_survives_hwpx_export() {
    let mut source = DocumentCore::new_empty();
    source.create_blank_document_native().expect("blank document");
    source.insert_text_native(0, 0, 0, "alpha beta alpha").expect("insert source text");
    let bytes = source.export_hwpx_native().expect("export synthetic HWPX");
    let mut service = NativeDocumentService::new();
    let handle = service.open_bytes(&bytes).expect("open synthetic document");
    let result = service.replace_all(handle, "alpha", "gamma", true).expect("replace text");
    assert!(result.contains("\"count\":2"), "unexpected result: {result}");
    let edited = service.export_hwpx(handle).expect("export edited HWPX");
    let mut reopened = DocumentCore::from_bytes(&edited).expect("reopen edited HWPX");
    let verify = reopened.replace_all_native("gamma", "delta", true).expect("verify replacement persisted");
    assert!(verify.contains("\"count\":2"), "replacement not persisted: {verify}");
}

#[test]
fn public_fixture_reports_page_info_renders_png_and_exports_hwpx() {
    let path = std::env::var("HWP_TEST_FIXTURE").expect("HWP_TEST_FIXTURE must be set");
    let bytes = std::fs::read(path).expect("read public HWP fixture");
    let mut service = NativeDocumentService::new();
    let handle = service.open_bytes(&bytes).expect("open public HWP fixture");
    let pages = service.page_count(handle).expect("page count");
    assert!(pages > 0);
    let (width, height) = service.page_info(handle, 0).expect("page info");
    assert!(width.is_finite() && width > 0.0);
    assert!(height.is_finite() && height > 0.0);
    let request = RenderRequest::new(0, 1.0, 4096, 16_000_000).unwrap();
    let png = service.render_page_png(handle, request).expect("render png");
    assert!(png.starts_with(&[0x89, b'P', b'N', b'G', 0x0d, 0x0a, 0x1a, 0x0a]));
    let hwpx = service.export_hwpx(handle).expect("export HWPX");
    assert!(hwpx.starts_with(b"PK"));
    let roundtrip = service.open_bytes(&hwpx).expect("reopen exported HWPX");
    assert_eq!(service.page_count(roundtrip).expect("roundtrip page count"), pages);
    service.close(roundtrip).expect("close roundtrip HWPX");
    assert!(service.page_info(handle, pages).is_err());
    service.close(handle).expect("close public HWP fixture");
}

#[test]
fn svg_render_preserves_korean_text_and_page_geometry() {
    let mut source = DocumentCore::new_empty();
    source.create_blank_document_native().expect("blank document");
    source.insert_text_native(0, 0, 0, "한글 표 레이아웃 테스트").expect("insert korean text");
    let bytes = source.export_hwpx_native().expect("export synthetic HWPX");
    let mut service = NativeDocumentService::new();
    let handle = service.open_bytes(&bytes).expect("open synthetic HWPX");
    let svg = service.render_page_svg(handle, 0).expect("render svg");
    assert!(svg.contains("<svg"), "SVG root missing");
    assert!(svg.contains("한글"), "Korean text must remain text in SVG");
    assert!(svg.contains("viewBox"), "page geometry viewBox missing");
    service.close(handle).expect("close document");
}
