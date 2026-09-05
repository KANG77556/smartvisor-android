use crate::document_registry::{DocumentRegistry, NativeError};
use crate::render_request::RenderRequest;
use rhwp::DocumentCore;

#[derive(Debug)]
pub enum NativeServiceError {
    InvalidHandle(u64),
    Parse(String),
    Render(String),
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

    pub fn page_count(&self, handle: u64) -> Result<u32, NativeServiceError> {
        self.documents.get(handle).map(|d| d.page_count()).map_err(map_registry_error)
    }

    pub fn page_info(&self, handle: u64, _page_index: u32) -> Result<(f64, f64), NativeServiceError> {
        self.documents.get(handle).map_err(map_registry_error)?;
        Err(NativeServiceError::Render("page info not implemented".to_string()))
    }

    pub fn render_page_png(&self, handle: u64, _request: RenderRequest) -> Result<Vec<u8>, NativeServiceError> {
        self.documents.get(handle).map_err(map_registry_error)?;
        Err(NativeServiceError::Render("page render not implemented".to_string()))
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
