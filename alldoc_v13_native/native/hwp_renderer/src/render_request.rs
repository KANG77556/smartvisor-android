#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum RenderRequestError {
    InvalidScale,
    InvalidDimension,
    InvalidPixels,
}

#[derive(Debug, Clone, Copy, PartialEq)]
pub struct RenderRequest {
    pub page_index: u32,
    pub scale: f64,
    pub max_dimension: i32,
    pub max_pixels: u64,
}

impl RenderRequest {
    pub fn new(page_index: u32, scale: f64, max_dimension: i32, max_pixels: u64) -> Result<Self, RenderRequestError> {
        if !scale.is_finite() || scale <= 0.0 || scale > 8.0 {
            return Err(RenderRequestError::InvalidScale);
        }
        if !(1..=16_384).contains(&max_dimension) {
            return Err(RenderRequestError::InvalidDimension);
        }
        if !(1..=67_108_864).contains(&max_pixels) {
            return Err(RenderRequestError::InvalidPixels);
        }
        Ok(Self { page_index, scale, max_dimension, max_pixels })
    }
}
