use std::collections::HashMap;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum NativeError {
    InvalidHandle(u64),
}

pub struct DocumentRegistry<T> {
    next_handle: u64,
    documents: HashMap<u64, T>,
}

impl<T> DocumentRegistry<T> {
    pub fn new() -> Self {
        Self { next_handle: 1, documents: HashMap::new() }
    }

    pub fn insert(&mut self, document: T) -> u64 {
        let handle = self.next_handle;
        self.next_handle = self.next_handle.saturating_add(1).max(1);
        self.documents.insert(handle, document);
        handle
    }

    pub fn get(&self, handle: u64) -> Result<&T, NativeError> {
        self.documents.get(&handle).ok_or(NativeError::InvalidHandle(handle))
    }

    pub fn get_mut(&mut self, handle: u64) -> Result<&mut T, NativeError> {
        self.documents.get_mut(&handle).ok_or(NativeError::InvalidHandle(handle))
    }

    pub fn remove(&mut self, handle: u64) -> Result<T, NativeError> {
        self.documents.remove(&handle).ok_or(NativeError::InvalidHandle(handle))
    }

    pub fn len(&self) -> usize { self.documents.len() }
}

impl<T> Default for DocumentRegistry<T> {
    fn default() -> Self { Self::new() }
}
