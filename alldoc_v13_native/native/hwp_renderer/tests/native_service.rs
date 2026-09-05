use hwp_renderer::native_service::{NativeDocumentService, NativeServiceError};

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
}
