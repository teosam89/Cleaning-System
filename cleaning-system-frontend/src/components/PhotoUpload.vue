<template>
  <div class="photo-upload-component">
    <div class="upload-header">
      <h4 class="upload-title">
        <el-icon><Camera /></el-icon>
        {{ uploadTitle }}
      </h4>
      <p class="upload-description">
        {{ uploadDescription }}
      </p>
    </div>

    <!-- Upload Area -->
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="uploadAction"
      :headers="uploadHeaders"
      :data="uploadData"
      :multiple="!isAvatarMode"
      :limit="effectiveMaxFiles"
      :accept="effectiveAcceptedTypes"
      :before-upload="beforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :auto-upload="false"
      list-type="picture-card"
      class="photo-uploader"
    >
      <div class="upload-trigger">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <div class="upload-text">
          {{ isAvatarMode ? "Add Avatar" : "Add Photos" }}
        </div>
      </div>
    </el-upload>

    <!-- Upload Controls -->
    <div class="upload-controls" v-if="fileList.length > 0">
      <div class="upload-info">
        <span class="file-count"
          >{{ fileList.length }}/{{ effectiveMaxFiles }}
          {{ isAvatarMode ? "avatar" : "photos" }} selected</span
        >
        <span class="file-size"
          >{{ isAvatarMode ? "Size" : "Total size" }}:
          {{ formatFileSize(totalSize) }}</span
        >
      </div>
      <div class="upload-actions">
        <el-button @click="clearAllPhotos" class="clear-btn">
          <el-icon><Delete /></el-icon>
          {{ isAvatarMode ? "Clear" : "Clear All" }}
        </el-button>
        <el-button
          type="primary"
          @click="uploadPhotos"
          :loading="uploading"
          :disabled="fileList.length === 0"
          class="upload-btn"
        >
          <el-icon><Upload /></el-icon>
          {{
            uploading
              ? "Preparing..."
              : isAvatarMode
              ? "Prepare Avatar"
              : "Prepare Photos"
          }}
        </el-button>
      </div>
    </div>

    <!-- Upload Progress -->
    <div class="upload-progress" v-if="uploading">
      <el-progress
        :percentage="uploadProgress"
        :status="uploadStatus"
        :stroke-width="8"
      >
        <template #default="{ percentage }">
          <span class="progress-text">{{ percentage }}%</span>
        </template>
      </el-progress>
    </div>

    <!-- Local Preview Images (Selected but not prepared yet) -->
    <div class="local-preview-images" v-if="localPreviewImages.length > 0">
      <h5 class="local-preview-title">
        <el-icon><View /></el-icon>
        Selected Images ({{ localPreviewImages.length }})
      </h5>
      <div class="images-grid">
        <div
          v-for="image in localPreviewImages"
          :key="image.id"
          class="image-item local-preview"
        >
          <div class="image-container">
            <img
              :src="image.localUrl"
              :alt="image.originalName"
              class="uploaded-image"
              @click="previewImage(image)"
            />
            <div class="image-overlay">
              <el-button
                type="primary"
                size="small"
                circle
                @click="previewImage(image)"
              >
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                circle
                @click="removeLocalImage(image)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div class="local-badge">
              <el-tag size="small" type="warning">Selected</el-tag>
            </div>
          </div>
          <div class="image-info">
            <span class="image-name">{{ image.originalName }}</span>
            <span class="image-size">{{ formatFileSize(image.fileSize) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Ready to Upload Images (Prepared but task not completed) -->
    <div class="ready-to-upload-images" v-if="readyToUploadImages.length > 0">
      <h5 class="ready-to-upload-title">
        <el-icon><Upload /></el-icon>
        Ready for Upload - Will upload when task is completed ({{ readyToUploadImages.length }})
      </h5>
      <div class="images-grid">
        <div
          v-for="image in readyToUploadImages"
          :key="image.id"
          class="image-item ready-to-upload"
        >
          <div class="image-container">
            <img
              :src="image.localUrl"
              :alt="image.originalName"
              class="uploaded-image"
              @click="previewImage(image)"
            />
            <div class="image-overlay">
              <el-button
                type="primary"
                size="small"
                circle
                @click="previewImage(image)"
              >
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                circle
                @click="removeReadyImage(image)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div class="ready-badge">
              <el-tag size="small" type="success">Ready</el-tag>
            </div>
          </div>
          <div class="image-info">
            <span class="image-name">{{ image.originalName }}</span>
            <span class="image-size">{{ formatFileSize(image.fileSize) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Existing Images Display -->
    <div class="existing-images" v-if="allImages.length > 0">
      <h5 class="existing-images-title">
        <el-icon><Picture /></el-icon>
        Uploaded Images ({{ allImages.length }})
      </h5>
      <div class="images-grid">
        <div
          v-for="image in allImages"
          :key="image.imageId || image.id"
          class="image-item"
        >
          <div class="image-container">
            <img
              :src="image.publicUrl || image.url"
              :alt="image.originalName || image.filename"
              class="uploaded-image"
              @click="previewImage(image)"
            />
            <div class="image-overlay">
              <el-button
                type="primary"
                size="small"
                circle
                @click="previewImage(image)"
              >
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                circle
                @click="deleteImage(image)"
                v-if="image.imageId"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="image-info">
            <span class="image-name">{{
              image.originalName || image.filename
            }}</span>
            <span class="image-size" v-if="image.fileSize">{{
              formatFileSize(image.fileSize)
            }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Preview Dialog -->
    <el-dialog
      v-model="previewVisible"
      title="Photo Preview"
      width="60%"
      :append-to-body="true"
    >
      <img
        v-if="previewUrl"
        :src="previewUrl"
        alt="Preview"
        class="preview-image"
      />
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Camera,
  Plus,
  Upload,
  Delete,
  Picture,
  ZoomIn,
  View,
} from "@element-plus/icons-vue";
import { AuthUtils } from "@/utils/auth";

export default {
  name: "PhotoUpload",
  components: {
    Camera,
    Plus,
    Upload,
    Delete,
    Picture,
    ZoomIn,
    View,
  },
  props: {
    taskId: {
      type: [String, Number],
      required: false,
    },
    userId: {
      type: [String, Number],
      required: false,
    },
    uploadType: {
      type: String,
      default: "task", // "task" or "avatar"
      validator: (value) => ["task", "avatar"].includes(value),
    },
    maxFiles: {
      type: Number,
      default: 5,
    },
    maxFileSize: {
      type: Number,
      default: 5 * 1024 * 1024, // 5MB
    },
    acceptedTypes: {
      type: String,
      default: "image/jpeg,image/png,image/gif",
    },
    existingImages: {
      type: Array,
      default: () => [],
    },
    currentAvatar: {
      type: String,
      default: null,
    },
  },
  emits: ["upload-success", "upload-error", "photos-changed", "image-deleted"],
  setup(props, { emit }) {
    // Reactive data
    const uploadRef = ref();
    const fileList = ref([]);
    const uploading = ref(false);
    const uploadProgress = ref(0);
    const uploadStatus = ref("");
    const previewVisible = ref(false);
    const previewUrl = ref("");
    const uploadedPhotos = ref([]);
    const localPreviewImages = ref([]);
    const readyToUploadImages = ref([]);

    // Computed properties
    const uploadTitle = computed(() => {
      return props.uploadType === "avatar"
        ? "Profile Avatar"
        : "Task Completion Photos (Optional)";
    });

    const uploadDescription = computed(() => {
      return props.uploadType === "avatar"
        ? "Upload a profile picture to personalize your account. Only JPEG and PNG files are supported."
        : "Upload photos to document the completed work. This helps maintain quality records.";
    });

    const uploadAction = computed(() => {
      if (props.uploadType === "avatar") {
        return `/api/upload/profile/${props.userId}`;
      }
      return `/api/upload/task-completion/${props.taskId}`;
    });

    const isAvatarMode = computed(() => {
      return props.uploadType === "avatar";
    });

    const effectiveMaxFiles = computed(() => {
      return isAvatarMode.value ? 1 : props.maxFiles;
    });

    const effectiveMaxFileSize = computed(() => {
      return isAvatarMode.value ? 2 * 1024 * 1024 : props.maxFileSize; // 2MB for avatar, 5MB for task
    });

    const effectiveAcceptedTypes = computed(() => {
      return isAvatarMode.value ? "image/jpeg,image/png" : props.acceptedTypes;
    });

    const uploadHeaders = computed(() => {
      const token = AuthUtils.getToken();
      return token ? { Authorization: `Bearer ${token}` } : {};
    });

    const uploadData = computed(() => {
      return {
        taskId: props.taskId,
        uploadType: "completion",
      };
    });

    const totalSize = computed(() => {
      return fileList.value.reduce((total, file) => {
        return total + (file.raw?.size || file.size || 0);
      }, 0);
    });

    // Only show truly uploaded images in the "Uploaded Images" section
    const allImages = computed(() => {
      return [...props.existingImages, ...uploadedPhotos.value];
    });

    // Separate computed property for images that should be shown in preview
    const allPreviewImages = computed(() => {
      return [...props.existingImages, ...uploadedPhotos.value, ...localPreviewImages.value, ...readyToUploadImages.value];
    });

    // Get images ready for upload when task is completed
    const getReadyImages = () => {
      return readyToUploadImages.value;
    };

    // Methods
    const beforeUpload = (file) => {
      // Check file type
      const isValidType = effectiveAcceptedTypes.value
        .split(",")
        .some((type) => file.type.includes(type.trim()));

      if (!isValidType) {
        const fileTypeMsg = isAvatarMode.value
          ? "Please upload valid image files (JPEG, PNG only)"
          : "Please upload valid image files (JPEG, PNG, GIF)";
        ElMessage.error(fileTypeMsg);
        return false;
      }

      // Check file size
      if (file.size > effectiveMaxFileSize.value) {
        ElMessage.error(
          `File size cannot exceed ${formatFileSize(
            effectiveMaxFileSize.value
          )}`
        );
        return false;
      }

      // Avatar specific validations
      if (isAvatarMode.value) {
        // Check minimum size for avatars
        if (file.size < 1024) {
          ElMessage.error("Avatar file is too small. Minimum size is 1KB");
          return false;
        }
      }

      return true;
    };

    const handleUploadSuccess = (response) => {
      console.log("Upload success:", response);
      // This method is kept for Element Plus compatibility but not used in manual upload mode
    };

    // eslint-disable-next-line no-unused-vars
    const handleUploadError = (error, file, fileList) => {
      console.error("Upload error:", error);
      ElMessage.error("Failed to upload photo. Please try again.");
      emit("upload-error", error);
    };

    // eslint-disable-next-line no-unused-vars
    const handleRemove = (file, fileList) => {
      console.log("File removed:", file.name);
    };

    // eslint-disable-next-line no-unused-vars
    const handleExceed = (files, fileList) => {
      const itemName = isAvatarMode.value ? "avatar" : "photos";
      ElMessage.warning(
        `You can only upload up to ${effectiveMaxFiles.value} ${itemName}`
      );
    };

    // Avatar upload method
    const uploadAvatarDirectly = async () => {
      if (fileList.value.length === 0) {
        ElMessage.warning("Please select an avatar file first");
        return;
      }

      const file = fileList.value[0];
      if (!file.raw) {
        ElMessage.error("Invalid file selected");
        return;
      }

      try {
        uploading.value = true;
        uploadProgress.value = 10;

        const formData = new FormData();
        formData.append('file', file.raw);

        const token = AuthUtils.getToken();
        const response = await fetch(`/api/upload/profile/${props.userId}`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`
          },
          body: formData
        });

        uploadProgress.value = 80;

        const result = await response.json();

        if (response.ok && result.success) {
          uploadProgress.value = 100;
          uploadStatus.value = "success";

          // Clear file list
          fileList.value = [];
          localPreviewImages.value = [];

          ElMessage.success("Avatar uploaded successfully!");

          // Emit success event with avatar URL
          emit("upload-success", {
            success: true,
            avatarUrl: result.avatarUrl || result.publicUrl,
            message: "Avatar uploaded successfully"
          });

        } else {
          throw new Error(result.message || "Upload failed");
        }

      } catch (error) {
        uploadProgress.value = 0;
        uploadStatus.value = "error";
        console.error("Avatar upload error:", error);
        ElMessage.error(error.message || "Failed to upload avatar");
        emit("upload-error", error);
      } finally {
        uploading.value = false;
      }
    };

    const uploadPhotos = async () => {
      const itemName = isAvatarMode.value ? "avatar" : "photos";
      if (fileList.value.length === 0) {
        ElMessage.warning(`Please select ${itemName} to prepare`);
        return;
      }

      try {
        uploading.value = true;
        uploadProgress.value = 0;
        uploadStatus.value = "";

        // Simulate preparation progress
        const progressInterval = setInterval(() => {
          if (uploadProgress.value < 100) {
            uploadProgress.value += 25;
          }
        }, 150);

        // Wait a moment to show progress
        await new Promise(resolve => setTimeout(resolve, 800));

        clearInterval(progressInterval);
        uploadProgress.value = 100;
        uploadStatus.value = "success";

        if (isAvatarMode.value) {
          // Avatar mode - direct upload
          await uploadAvatarDirectly();
        } else {
          // Move local preview images to ready-to-upload status
          const imagesToMove = [...localPreviewImages.value];

          // Clear local previews and move to ready status
          localPreviewImages.value = [];

          // Add to ready-to-upload with updated status
          readyToUploadImages.value = [...readyToUploadImages.value, ...imagesToMove.map(img => ({
            ...img,
            ready: true,
            status: 'ready'
          }))];

          ElMessage.success(`${imagesToMove.length} photo(s) prepared for upload. They will be uploaded when you complete the task.`);
          emit("photos-changed", allPreviewImages.value);
        }

        // Clear file list after preparation
        fileList.value = [];

      } catch (error) {
        uploadProgress.value = 0;
        uploadStatus.value = "error";
        console.error("Prepare error:", error);
        ElMessage.error(error.message || `Failed to prepare ${itemName}`);
      } finally {
        uploading.value = false;
      }
    };

    const clearAllPhotos = async () => {
      try {
        await ElMessageBox.confirm(
          "Are you sure you want to remove all selected photos?",
          "Clear Photos",
          {
            confirmButtonText: "Clear",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        // Clean up local URLs to prevent memory leaks
        localPreviewImages.value.forEach(img => {
          if (img.localUrl && img.localUrl.startsWith('blob:')) {
            URL.revokeObjectURL(img.localUrl);
          }
        });
        readyToUploadImages.value.forEach(img => {
          if (img.localUrl && img.localUrl.startsWith('blob:')) {
            URL.revokeObjectURL(img.localUrl);
          }
        });

        fileList.value = [];
        uploadedPhotos.value = [];
        localPreviewImages.value = [];
        readyToUploadImages.value = [];
        uploadProgress.value = 0;
        uploadStatus.value = "";

        emit("photos-changed", allPreviewImages.value);
        ElMessage.success("All photos cleared");
      } catch (error) {
        // User cancelled
      }
    };

    const formatFileSize = (bytes) => {
      if (bytes === 0) return "0 B";
      const k = 1024;
      const sizes = ["B", "KB", "MB", "GB"];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    };

    const previewPhoto = (file) => {
      previewUrl.value = file.url || URL.createObjectURL(file.raw);
      previewVisible.value = true;
    };

    const previewImage = (image) => {
      // Handle different image object structures
      let imageUrl = null;

      if (image.localUrl) {
        // Local preview image
        imageUrl = image.localUrl;
      } else if (image.publicUrl) {
        // Server uploaded image
        imageUrl = image.publicUrl;
      } else if (image.url) {
        // Alternative URL property
        imageUrl = image.url;
      } else {
        console.warn('No valid URL found for image:', image);
        ElMessage.warning('Unable to preview this image');
        return;
      }

      previewUrl.value = imageUrl;
      previewVisible.value = true;
    };

    const removeLocalImage = (image) => {
      // Remove local preview image
      const index = localPreviewImages.value.findIndex(img => img.id === image.id);
      if (index !== -1) {
        // Clean up the blob URL
        if (image.localUrl && image.localUrl.startsWith('blob:')) {
          URL.revokeObjectURL(image.localUrl);
        }

        // Remove from local preview array
        localPreviewImages.value.splice(index, 1);

        // Also remove from file list
        const fileIndex = fileList.value.findIndex(file =>
          file.name === image.originalName &&
          file.raw && file.raw.size === image.fileSize
        );
        if (fileIndex !== -1) {
          fileList.value.splice(fileIndex, 1);
        }

        emit("photos-changed", allPreviewImages.value);
        ElMessage.success("Image removed from selection");
      }
    };

    const removeReadyImage = (image) => {
      // Remove ready-to-upload image
      const index = readyToUploadImages.value.findIndex(img => img.id === image.id);
      if (index !== -1) {
        // Clean up the blob URL
        if (image.localUrl && image.localUrl.startsWith('blob:')) {
          URL.revokeObjectURL(image.localUrl);
        }

        // Remove from ready-to-upload array
        readyToUploadImages.value.splice(index, 1);

        emit("photos-changed", allPreviewImages.value);
        ElMessage.success("Image removed from ready queue");
      }
    };

    const deleteImage = async (image) => {
      if (!image.imageId) {
        ElMessage.error("Cannot delete this image");
        return;
      }

      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete "${image.originalName}"?`,
          "Delete Image",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        const token = AuthUtils.getToken();
        const response = await fetch(`/api/images/${image.imageId}`, {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        const result = await response.json();

        if (response.ok && result.success) {
          // Remove from uploaded photos
          const uploadedIndex = uploadedPhotos.value.findIndex(
            (img) => img.imageId === image.imageId
          );
          if (uploadedIndex !== -1) {
            uploadedPhotos.value.splice(uploadedIndex, 1);
          }

          ElMessage.success("Image deleted successfully");
          emit("image-deleted", image.imageId);
          emit("photos-changed", allImages.value);
        } else {
          throw new Error(result.message || "Delete failed");
        }
      } catch (error) {
        if (error.message !== "cancel") {
          console.error("Delete error:", error);
          ElMessage.error(error.message || "Failed to delete image");
        }
      }
    };

    // Public methods for parent component
    const getUploadedPhotos = () => {
      return uploadedPhotos.value;
    };

    const hasPhotos = () => {
      return fileList.value.length > 0 || uploadedPhotos.value.length > 0;
    };

    const resetUpload = () => {
      // Clean up local URLs to prevent memory leaks
      localPreviewImages.value.forEach(img => {
        if (img.localUrl && img.localUrl.startsWith('blob:')) {
          URL.revokeObjectURL(img.localUrl);
        }
      });
      readyToUploadImages.value.forEach(img => {
        if (img.localUrl && img.localUrl.startsWith('blob:')) {
          URL.revokeObjectURL(img.localUrl);
        }
      });

      fileList.value = [];
      uploadedPhotos.value = [];
      localPreviewImages.value = [];
      readyToUploadImages.value = [];
      uploadProgress.value = 0;
      uploadStatus.value = "";
      uploading.value = false;
    };

    // Watch for file list changes
    watch(
      fileList,
      (newFileList) => {
        // Generate local preview URLs for new files
        updateLocalPreviews(newFileList);
        emit("photos-changed", allPreviewImages.value);
      },
      { deep: true }
    );

    // Generate local preview URLs for selected files
    const updateLocalPreviews = (fileList) => {
      // Clean up old URLs to prevent memory leaks
      localPreviewImages.value.forEach(img => {
        if (img.localUrl && img.localUrl.startsWith('blob:')) {
          URL.revokeObjectURL(img.localUrl);
        }
      });

      // Generate new local URLs
      localPreviewImages.value = fileList.map((file, index) => {
        if (file.raw) {
          return {
            id: `local_${index}_${Date.now()}`,
            originalName: file.name,
            localUrl: URL.createObjectURL(file.raw),
            fileSize: file.raw.size,
            mimeType: file.raw.type,
            isLocal: true,
            uploaded: false,
            file: file.raw
          };
        }
        return null;
      }).filter(Boolean);
    };

    return {
      // Refs
      uploadRef,
      fileList,
      uploading,
      uploadProgress,
      uploadStatus,
      previewVisible,
      previewUrl,
      uploadedPhotos,
      localPreviewImages,
      readyToUploadImages,

      // Computed
      uploadTitle,
      uploadDescription,
      uploadAction,
      uploadHeaders,
      uploadData,
      totalSize,
      allImages,
      allPreviewImages,
      isAvatarMode,
      effectiveMaxFiles,
      effectiveMaxFileSize,
      effectiveAcceptedTypes,

      // Methods
      beforeUpload,
      handleUploadSuccess,
      handleUploadError,
      handleRemove,
      handleExceed,
      uploadPhotos,
      uploadAvatarDirectly,
      clearAllPhotos,
      formatFileSize,
      previewPhoto,
      previewImage,
      removeLocalImage,
      removeReadyImage,
      deleteImage,

      // Public methods
      getUploadedPhotos,
      getReadyImages,
      hasPhotos,
      resetUpload,
      updateLocalPreviews,
    };
  },
};
</script>

<style scoped>
.photo-upload-component {
  width: 100%;
}

.upload-header {
  margin-bottom: 1.5rem;
}

.upload-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.upload-description {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
  line-height: 1.4;
}

.photo-uploader {
  width: 100%;
}

.photo-uploader :deep(.el-upload) {
  border: 2px dashed #d1d5db;
  border-radius: 0.75rem;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  width: 120px;
  height: 120px;
}

.photo-uploader :deep(.el-upload):hover {
  border-color: #10b981;
  background-color: #f0fdf4;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
}

.upload-icon {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.upload-text {
  font-size: 0.75rem;
  font-weight: 500;
}

.upload-controls {
  margin-top: 1rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 0.75rem;
  border: 1px solid #e5e7eb;
}

.upload-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.file-count {
  font-weight: 500;
}

.upload-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.clear-btn {
  color: #ef4444;
  border-color: #ef4444;
  border-radius: 0.5rem;
}

.clear-btn:hover {
  background: #ef4444;
  color: white;
}

.upload-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981;
  border-radius: 0.5rem;
}

.upload-btn:hover {
  background: linear-gradient(135deg, #059669, #047857);
  border-color: #059669;
}

.upload-progress {
  margin-top: 1rem;
}

.progress-text {
  font-weight: 600;
  color: #374151;
}

.preview-image {
  width: 100%;
  max-height: 60vh;
  object-fit: contain;
}

/* Local Preview Images Styles */
.local-preview-images {
  margin-top: 2rem;
}

.local-preview-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: #d97706;
  margin: 0 0 1rem 0;
}

.image-item.local-preview {
  border: 2px dashed #f59e0b;
  background: #fef3c7;
}

.local-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 1;
}

/* Ready to Upload Images Styles */
.ready-to-upload-images {
  margin-top: 2rem;
}

.ready-to-upload-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: #059669;
  margin: 0 0 1rem 0;
}

.image-item.ready-to-upload {
  border: 2px solid #10b981;
  background: #d1fae5;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
}

.ready-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 1;
}

/* Existing Images Styles */
.existing-images {
  margin-top: 2rem;
}

.existing-images-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 1rem 0;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
}

.image-item {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  overflow: hidden;
  transition: all 0.3s ease;
}

.image-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.image-container {
  position: relative;
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.uploaded-image:hover {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.image-info {
  padding: 0.75rem;
}

.image-name {
  display: block;
  font-size: 0.75rem;
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0.25rem;
}

.image-size {
  display: block;
  font-size: 0.625rem;
  color: #6b7280;
}

/* Responsive */
@media (max-width: 768px) {
  .upload-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .upload-actions {
    justify-content: stretch;
  }

  .upload-actions .el-button {
    flex: 1;
  }

  .photo-uploader :deep(.el-upload) {
    width: 100px;
    height: 100px;
  }

  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .image-container {
    height: 100px;
  }
}
</style>
