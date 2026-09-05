use hwp_renderer::document_registry::{DocumentRegistry, NativeError};

#[test]
fn removed_handle_is_rejected() {
    let mut registry = DocumentRegistry::<String>::new();
    let handle = registry.insert("doc".to_string());
    assert_eq!(registry.remove(handle).unwrap(), "doc");
    assert_eq!(registry.get(handle).unwrap_err(), NativeError::InvalidHandle(handle));
}

#[test]
fn unknown_handle_is_rejected() {
    let registry = DocumentRegistry::<String>::new();
    assert_eq!(registry.get(999).unwrap_err(), NativeError::InvalidHandle(999));
}
