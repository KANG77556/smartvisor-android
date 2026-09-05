use crate::document_registry::{DocumentRegistry, NativeError};
use crate::render_request::RenderRequest;
use rhwp::renderer::layer_renderer::RasterRenderOptions;
use rhwp::renderer::skia::SkiaLayerRenderer;
use rhwp::DocumentCore;

#[derive(Debug)]
pub enum NativeServiceError {
    InvalidHandle(u64),
    Parse(String),
    Render(String),
    Export(String),
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

    pub fn page_info(&self, handle: u64, page_index: u32) -> Result<(f64, f64), NativeServiceError> {
        let document = self.documents.get(handle).map_err(map_registry_error)?;
        let tree = document.build_page_layer_tree(page_index).map_err(|e| NativeServiceError::Render(e.to_string()))?;
        Ok((tree.page_width, tree.page_height))
    }

    pub fn render_page_png(&self, handle: u64, request: RenderRequest) -> Result<Vec<u8>, NativeServiceError> {
        let document = self.documents.get(handle).map_err(map_registry_error)?;
        let tree = document.build_page_layer_tree(request.page_index).map_err(|e| NativeServiceError::Render(e.to_string()))?;
        let renderer = SkiaLayerRenderer::new();
        let options = RasterRenderOptions {
            max_dimension: request.max_dimension,
            max_pixels: request.max_pixels,
            scale: request.scale,
            ..RasterRenderOptions::default()
        };
        renderer.render_raster_with_options(&tree, options)
            .map(|output| output.bytes)
            .map_err(|e| NativeServiceError::Render(e.to_string()))
    }

    pub fn export_hwpx(&self, handle: u64) -> Result<Vec<u8>, NativeServiceError> {
        let document = self.documents.get(handle).map_err(map_registry_error)?;
        document.export_hwpx_native().map_err(|e| NativeServiceError::Export(e.to_string()))
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
