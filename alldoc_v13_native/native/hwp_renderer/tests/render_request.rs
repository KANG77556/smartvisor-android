#[path = "../src/render_request.rs"]
mod render_request;

use render_request::{RenderRequest, RenderRequestError};

#[test]
fn accepts_bounded_request() {
    let r = RenderRequest::new(0, 1.5, 4096, 16_000_000).unwrap();
    assert_eq!(r.page_index, 0);
    assert_eq!(r.max_dimension, 4096);
}

#[test]
fn rejects_invalid_scale_and_bounds() {
    assert_eq!(RenderRequest::new(0, 0.0, 4096, 1).unwrap_err(), RenderRequestError::InvalidScale);
    assert_eq!(RenderRequest::new(0, f64::NAN, 4096, 1).unwrap_err(), RenderRequestError::InvalidScale);
    assert_eq!(RenderRequest::new(0, 9.0, 4096, 1).unwrap_err(), RenderRequestError::InvalidScale);
    assert_eq!(RenderRequest::new(0, 1.0, 0, 1).unwrap_err(), RenderRequestError::InvalidDimension);
    assert_eq!(RenderRequest::new(0, 1.0, 16385, 1).unwrap_err(), RenderRequestError::InvalidDimension);
    assert_eq!(RenderRequest::new(0, 1.0, 4096, 0).unwrap_err(), RenderRequestError::InvalidPixels);
    assert_eq!(RenderRequest::new(0, 1.0, 4096, 67_108_865).unwrap_err(), RenderRequestError::InvalidPixels);
}
