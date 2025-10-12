/**
 * Avatar utility functions for URL handling, validation, and display
 */

/**
 * Validates if a given URL is a valid avatar URL
 * @param {string} url - The avatar URL to validate
 * @returns {boolean} - True if URL is valid, false otherwise
 */
export function isValidAvatarUrl(url) {
  if (!url || typeof url !== "string") {
    return false;
  }

  try {
    const urlObj = new URL(url);

    // Check if it's a supported protocol
    if (!["http:", "https:", "data:"].includes(urlObj.protocol)) {
      return false;
    }

    // Check if it's a data URL with image type
    if (urlObj.protocol === "data:") {
      return url.startsWith("data:image/");
    }

    // Check if it's a valid image file extension for http/https URLs
    const pathname = urlObj.pathname.toLowerCase();
    const validExtensions = [".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"];

    // Allow URLs without extensions (API endpoints)
    if (!pathname.includes(".")) {
      return true;
    }

    return validExtensions.some((ext) => pathname.endsWith(ext));
  } catch (error) {
    return false;
  }
}

/**
 * Sanitizes an avatar URL to ensure it's safe to use
 * @param {string} url - The avatar URL to sanitize
 * @returns {string|null} - Sanitized URL or null if invalid
 */
export function sanitizeAvatarUrl(url) {
  if (!isValidAvatarUrl(url)) {
    return null;
  }

  try {
    // Remove any potential XSS or dangerous characters
    const cleanUrl = url.trim().replace(/[\s\n\r\t]/g, "");

    // Additional validation for potential XSS
    if (cleanUrl.includes("javascript:") || cleanUrl.includes("vbscript:")) {
      return null;
    }

    return cleanUrl;
  } catch (error) {
    console.warn("Error sanitizing avatar URL:", error);
    return null;
  }
}

/**
 * Generates initials from a full name for fallback display
 * @param {string} fullName - The user's full name
 * @param {number} maxInitials - Maximum number of initials (default: 2)
 * @returns {string} - Generated initials
 */
export function generateInitials(fullName, maxInitials = 2) {
  if (!fullName || typeof fullName !== "string") {
    return "?";
  }

  const names = fullName
    .trim()
    .split(/\s+/)
    .filter((name) => name.length > 0);

  if (names.length === 0) {
    return "?";
  }

  if (names.length === 1) {
    return names[0].charAt(0).toUpperCase();
  }

  // Take first letter of first name and first letter of last name
  const initials = [names[0].charAt(0), names[names.length - 1].charAt(0)]
    .slice(0, maxInitials)
    .map((initial) => initial.toUpperCase())
    .join("");

  return initials;
}

/**
 * Generates a default avatar URL using initial-based avatar services
 * @param {string} fullName - The user's full name
 * @param {object} options - Configuration options
 * @returns {string} - Generated avatar URL
 */
export function generateDefaultAvatar(fullName, options = {}) {
  const {
    size = 100,
    backgroundColor = "64748b",
    textColor = "ffffff",
    service = "ui-avatars", // 'ui-avatars' or 'dicebear'
  } = options;

  const initials = generateInitials(fullName);

  if (service === "dicebear") {
    // Use DiceBear Avatars service
    const seed = encodeURIComponent(fullName || "anonymous");
    return `https://api.dicebear.com/7.x/initials/svg?seed=${seed}&size=${size}&backgroundColor=${backgroundColor}&color=${textColor}`;
  }

  // Default: UI Avatars service
  const name = encodeURIComponent(initials);
  return `https://ui-avatars.com/api/?name=${name}&size=${size}&background=${backgroundColor}&color=${textColor}&bold=true&format=svg`;
}

/**
 * Gets the best available avatar URL with fallback options
 * @param {object} user - User object with potential avatar fields
 * @param {object} options - Configuration options
 * @returns {string} - The best available avatar URL
 */
export function getAvatarUrl(user, options = {}) {
  const {
    size = 100,
    useDefaultService = true,
    preferredField = "avatarUrl", // 'avatarUrl' or 'avatar'
  } = options;

  if (!user) {
    return generateDefaultAvatar("Anonymous User", { size });
  }

  // Try primary field first, then fallback field
  const primaryUrl = user[preferredField];
  const fallbackUrl =
    preferredField === "avatarUrl" ? user.avatar : user.avatarUrl;

  // Check primary URL
  if (sanitizeAvatarUrl(primaryUrl)) {
    return primaryUrl;
  }

  // Check fallback URL
  if (sanitizeAvatarUrl(fallbackUrl)) {
    return fallbackUrl;
  }

  // Generate default avatar if enabled
  if (useDefaultService) {
    return generateDefaultAvatar(user.fullName || user.username || "User", {
      size,
    });
  }

  // Return null if no default service
  return null;
}

/**
 * Optimizes avatar URL for display by adding appropriate parameters
 * @param {string} url - The avatar URL
 * @param {object} options - Optimization options
 * @returns {string} - Optimized avatar URL
 */
export function optimizeAvatarUrl(url, options = {}) {
  if (!url || !isValidAvatarUrl(url)) {
    return url;
  }

  const {
    size = null,
    quality = null,
    format = null,
    enableCaching = true,
  } = options;

  try {
    const urlObj = new URL(url);

    // Only optimize API endpoints (not external services)
    if (!urlObj.pathname.startsWith("/api/files/profile/")) {
      return url;
    }

    const params = new URLSearchParams(urlObj.search);

    // Add size parameter if specified
    if (size && !params.has("size")) {
      params.set("w", size.toString());
      params.set("h", size.toString());
    }

    // Add quality parameter if specified
    if (quality && !params.has("q")) {
      params.set("q", quality.toString());
    }

    // Add format parameter if specified
    if (format && !params.has("f")) {
      params.set("f", format);
    }

    // Add cache busting parameter if caching is disabled
    if (!enableCaching) {
      params.set("t", Date.now().toString());
    }

    // Rebuild URL with parameters
    urlObj.search = params.toString();
    return urlObj.toString();
  } catch (error) {
    console.warn("Error optimizing avatar URL:", error);
    return url;
  }
}

/**
 * Preloads avatar images to improve display performance
 * @param {string[]} urls - Array of avatar URLs to preload
 * @returns {Promise[]} - Array of promises for preloading
 */
export function preloadAvatars(urls) {
  if (!Array.isArray(urls)) {
    return [];
  }

  return urls
    .filter((url) => isValidAvatarUrl(url))
    .map((url) => {
      return new Promise((resolve, reject) => {
        const img = new Image();

        img.onload = () => {
          console.debug(`Avatar preloaded: ${url}`);
          resolve(url);
        };

        img.onerror = (error) => {
          console.warn(`Failed to preload avatar: ${url}`, error);
          reject(error);
        };

        // Set a timeout for preloading
        setTimeout(() => {
          if (!img.complete) {
            reject(new Error(`Avatar preload timeout: ${url}`));
          }
        }, 5000);

        img.src = url;
      });
    });
}

/**
 * Creates a data URL from a File object for preview purposes
 * @param {File} file - The image file
 * @returns {Promise<string>} - Promise resolving to data URL
 */
export function createPreviewUrl(file) {
  return new Promise((resolve, reject) => {
    if (!file || !file.type.startsWith("image/")) {
      reject(new Error("Invalid image file"));
      return;
    }

    const reader = new FileReader();

    reader.onload = (event) => {
      resolve(event.target.result);
    };

    reader.onerror = (error) => {
      reject(error);
    };

    reader.readAsDataURL(file);
  });
}

/**
 * Validates avatar file before upload
 * @param {File} file - The image file to validate
 * @param {object} options - Validation options
 * @returns {object} - Validation result
 */
export function validateAvatarFile(file, options = {}) {
  const {
    maxSize = 2 * 1024 * 1024, // 2MB
    allowedTypes = ["image/jpeg", "image/png"],
    minWidth = 50,
    minHeight = 50,
    maxWidth = 2000,
    maxHeight = 2000,
  } = options;

  if (!file) {
    return {
      valid: false,
      error: "No file provided",
    };
  }

  // Check file type
  if (!allowedTypes.includes(file.type)) {
    return {
      valid: false,
      error: `File type not allowed. Supported formats: ${allowedTypes.join(
        ", "
      )}`,
    };
  }

  // Check file size
  if (file.size > maxSize) {
    const maxSizeMB = (maxSize / (1024 * 1024)).toFixed(1);
    return {
      valid: false,
      error: `File size exceeds ${maxSizeMB}MB limit`,
    };
  }

  // Check minimum file size (to avoid empty files)
  if (file.size < 1024) {
    return {
      valid: false,
      error: "File is too small. Minimum size is 1KB",
    };
  }

  // Return promise for dimension validation
  return new Promise((resolve) => {
    const img = new Image();

    img.onload = () => {
      if (img.width < minWidth || img.height < minHeight) {
        resolve({
          valid: false,
          error: `Image dimensions too small. Minimum: ${minWidth}x${minHeight}px`,
        });
        return;
      }

      if (img.width > maxWidth || img.height > maxHeight) {
        resolve({
          valid: false,
          error: `Image dimensions too large. Maximum: ${maxWidth}x${maxHeight}px`,
        });
        return;
      }

      resolve({
        valid: true,
        dimensions: {
          width: img.width,
          height: img.height,
        },
        aspectRatio: img.width / img.height,
      });
    };

    img.onerror = () => {
      resolve({
        valid: false,
        error: "Invalid image file or corrupted data",
      });
    };

    img.src = URL.createObjectURL(file);
  });
}

/**
 * Default avatar configuration
 */
export const AVATAR_CONFIG = {
  sizes: {
    small: 32,
    medium: 64,
    large: 100,
    xlarge: 150,
  },
  defaultBackgrounds: [
    "64748b",
    "059669",
    "7c3aed",
    "d97706",
    "dc2626",
    "2563eb",
    "7c2d12",
    "166534",
  ],
  supportedFormats: ["jpeg", "png", "webp"],
  maxFileSize: 2 * 1024 * 1024, // 2MB
  allowedMimeTypes: ["image/jpeg", "image/png"],
};

/**
 * Avatar API and caching functionality
 */

import { reactive } from 'vue'
import { API } from '@/utils/request'

// Global avatar cache to share across components
const avatarCache = reactive({})

/**
 * Get user avatar URL from cache or fetch from API
 * @param {number|string} userId - The user ID
 * @returns {string|null} - Avatar URL or null if no avatar
 */
export function getUserAvatarFromAPI(userId) {
  if (!userId) return null

  // Return cached avatar URL if available
  if (avatarCache[userId] !== undefined) {
    return avatarCache[userId]
  }

  // Initialize with null to prevent multiple simultaneous requests
  avatarCache[userId] = null

  // Fetch avatar asynchronously
  API.get(`/api/profile/${userId}/avatar`)
    .then(response => {
      if (response.data.success && response.data.hasAvatar) {
        avatarCache[userId] = response.data.avatarUrl
      } else {
        avatarCache[userId] = null
      }
    })
    .catch(error => {
      console.error(`Failed to fetch avatar for user ${userId}:`, error)
      avatarCache[userId] = null
    })

  return avatarCache[userId]
}

/**
 * Get comprehensive user avatar with fallback generation
 * @param {object|number} userOrId - User object or user ID
 * @param {object} options - Configuration options
 * @returns {string} - Best available avatar URL with fallbacks
 */
export function getComprehensiveAvatar(userOrId, options = {}) {
  const { size = 64, userName = null } = options

  // Handle user ID only
  if (typeof userOrId === 'number' || typeof userOrId === 'string') {
    const apiAvatar = getUserAvatarFromAPI(userOrId)
    if (apiAvatar) {
      return optimizeAvatarUrl(apiAvatar, { size })
    }
    // Fallback to generated avatar with user name if provided
    return generateDefaultAvatar(userName || `User ${userOrId}`, { size })
  }

  // Handle user object
  if (userOrId && typeof userOrId === 'object') {
    // Try API first if user has an ID
    if (userOrId.userId) {
      const apiAvatar = getUserAvatarFromAPI(userOrId.userId)
      if (apiAvatar) {
        return optimizeAvatarUrl(apiAvatar, { size })
      }
    }

    // Fallback to existing avatar utilities
    return getAvatarUrl(userOrId, { size, useDefaultService: true })
  }

  // Ultimate fallback
  return generateDefaultAvatar('Anonymous', { size })
}

/**
 * Clear avatar cache for a specific user
 * @param {number|string} userId - The user ID
 */
export function clearUserAvatarCache(userId) {
  if (userId && avatarCache[userId] !== undefined) {
    delete avatarCache[userId]
  }
}

/**
 * Clear all avatar cache
 */
export function clearAllAvatarCache() {
  Object.keys(avatarCache).forEach(key => delete avatarCache[key])
}

/**
 * Get the avatar cache for debugging
 * @returns {object} - The avatar cache
 */
export function getAvatarCache() {
  return avatarCache
}

export default {
  isValidAvatarUrl,
  sanitizeAvatarUrl,
  generateInitials,
  generateDefaultAvatar,
  getAvatarUrl,
  optimizeAvatarUrl,
  preloadAvatars,
  createPreviewUrl,
  validateAvatarFile,
  getUserAvatarFromAPI,
  getComprehensiveAvatar,
  clearUserAvatarCache,
  clearAllAvatarCache,
  getAvatarCache,
  AVATAR_CONFIG,
};
