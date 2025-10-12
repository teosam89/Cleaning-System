/**
 * Simple attendance service - basic time recording only
 * No GPS tracking or location validation
 */

/**
 * Simple check-in function - just records timestamp
 * Location validation has been removed as requested
 */
export async function performCheckIn() {
  const timestamp = new Date().toISOString();
  return {
    success: true,
    timestamp: timestamp,
    location: "Office Location", // Static location for simplicity
    message: "Checked in successfully",
  };
}

/**
 * Simple check-out function - just records timestamp
 * Location validation has been removed as requested
 */
export async function performCheckOut() {
  const timestamp = new Date().toISOString();
  return {
    success: true,
    timestamp: timestamp,
    location: "Office Location", // Static location for simplicity
    message: "Checked out successfully",
  };
}

/**
 * Get current timestamp for attendance
 */
export function getCurrentTimestamp() {
  return new Date().toISOString();
}

/**
 * Format timestamp for display
 */
export function formatTimestamp(timestamp) {
  return new Date(timestamp).toLocaleString();
}

// Legacy compatibility exports (minimal functionality)
export const getCurrentLocation = async () => ({
  latitude: null,
  longitude: null,
  address: "Office Location",
  timestamp: Date.now(),
});

export const isWithinCheckInRange = () => ({
  allowed: true,
  message: "Location validation disabled",
});

export const getOfficeLocation = () => ({
  name: "Office Location",
});

export const getLocationDebugInfo = () => ({
  message: "Location tracking has been disabled",
});

// Remove GPS-specific functions
export const getGPSLocation = getCurrentLocation;
export const getEnhancedLocation = getCurrentLocation;
export const getMultipleSampleGPSPosition = getCurrentLocation;
export const getGPSStatusInfo = () => ({ status: "disabled" });
