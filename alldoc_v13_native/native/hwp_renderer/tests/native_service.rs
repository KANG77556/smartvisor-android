use hwp_renderer::native_service::{NativeDocumentService, NativeServiceError};
use hwp_renderer::render_request::RenderRequest;

#[test]
fn malformed_document_is_rejected_without_allocating_handle() {
    let mut service = NativeDocumentService::new();
    let err = service.open_bytes(b"not-an-hwp").unwrap_err();
    assert!(matches!(err, NativeServiceError::Parse(_)));
    assert_eq!(service.open_document_count(), 0);
}

#[test]
fn invalid_handle_is_rejected() {
    let service = NativeDocumentService::new();
    assert!(matches!(service.page_count(42), Err(NativeServiceError::InvalidHandle(42))));
    assert!(matches!(service.page_info(42, 0), Err(NativeServiceError::InvalidHandle(42))));
    let request = RenderRequest::new(0, 1.0, 4096, 16_000_000).unwrap();
    assert!(matches!(service.render_page_png(42, request), Err(NativeServiceError::InvalidHandle(42))));
    assert!(matches!(service.export_hwpx(42), Err(NativeServiceError::InvalidHandle(42))));
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
