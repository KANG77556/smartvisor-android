use crate::document_registry::{DocumentRegistry, NativeError};
use rhwp::DocumentCore;

#[derive(Debug)]
pub enum NativeServiceError {
    InvalidHandle(u64),
    Parse(String),
}

pub struct NativeDocumentService {
    documents: DocumentRegistry<DocumentCore>,
}

impl NativeDocumentService {
    pub fn new() -> Self { Self { documents: DocumentRegistry::new() } }

    pub fn open_bytes(&mut self, bytes: &[u8]) -> Result<u64, NativeServiceError> {
        let document = DocumentCore::from_bytes(bytes).map_err(|e| NativeServiceError::Parse(e.to_string()))?;
        Ok(self.documents.insert(document))
    }

    pub fn page_count(&self, handle: u64) -> Result<usize, NativeServiceError> {
        self.documents.get(handle).map(|d| d.page_count()).map_err(map_registry_error)
    }

    pub fn close(&mut self, handle: u64) -> Result<(), NativeServiceError> {
        self.documents.remove(handle).map(|_| ()).map_err(map_registry_error)
    }

    pub fn open_document_count(&self) -> usize { self.documents.len() }
}

impl Default for NativeDocumentService {
    fn default() -> Self { Self::new() }
}

fn map_registry_error(error: NativeError) -> NativeServiceError {
    match error { NativeError::InvalidHandle(handle) => NativeServiceError::InvalidHandle(handle) }
}
