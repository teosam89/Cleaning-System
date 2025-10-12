<template>
  <div class="task-completion-gallery" v-if="images.length > 0">
    <div class="gallery-header">
      <h4 class="gallery-title">
        <el-icon><Camera /></el-icon>
        Task Completion Photos ({{ images.length }})
      </h4>
      <p class="gallery-subtitle">
        Photos uploaded when this task was completed
      </p>
    </div>

    <!-- Image Grid -->
    <div class="images-grid">
      <div
        v-for="(image, index) in images"
        :key="image.imageId || image.id || index"
        class="image-item"
        @click="openFullscreen(index)"
      >
        <div class="image-container">
          <img
            :src="image.publicUrl || image.url"
            :alt="image.originalName || `Completion photo ${index + 1}`"
            class="completion-image"
            loading="lazy"
          />
          <div class="image-overlay">
            <el-button
              type="primary"
              size="large"
              circle
              class="view-btn"
            >
              <el-icon><ZoomIn /></el-icon>
            </el-button>
          </div>
          <div class="image-info">
            <span class="image-name">{{ image.originalName || `Photo ${index + 1}` }}</span>
            <span class="image-size" v-if="image.fileSize">{{ formatFileSize(image.fileSize) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Fullscreen Gallery -->
    <el-dialog
      v-model="fullscreenVisible"
      :title="`Task Completion Photos (${currentIndex + 1}/${images.length})`"
      width="90%"
      :append-to-body="true"
      class="fullscreen-dialog"
    >
      <div class="fullscreen-gallery">
        <div class="gallery-main">
          <img
            :src="images[currentIndex]?.publicUrl || images[currentIndex]?.url"
            :alt="images[currentIndex]?.originalName || `Photo ${currentIndex + 1}`"
            class="fullscreen-image"
          />
        </div>

        <!-- Navigation -->
        <div class="gallery-nav" v-if="images.length > 1">
          <el-button
            @click="previousImage"
            :disabled="currentIndex === 0"
            size="large"
            circle
            class="nav-btn prev-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button
            @click="nextImage"
            :disabled="currentIndex === images.length - 1"
            size="large"
            circle
            class="nav-btn next-btn"
          >
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <!-- Thumbnail Strip -->
        <div class="thumbnail-strip" v-if="images.length > 1">
          <div
            v-for="(image, index) in images"
            :key="image.imageId || image.id || index"
            class="thumbnail-item"
            :class="{ active: index === currentIndex }"
            @click="currentIndex = index"
          >
            <img
              :src="image.publicUrl || image.url"
              :alt="image.originalName || `Thumbnail ${index + 1}`"
              class="thumbnail-image"
            />
          </div>
        </div>

        <!-- Image Info -->
        <div class="fullscreen-info">
          <div class="info-item">
            <span class="info-label">File:</span>
            <span class="info-value">{{ images[currentIndex]?.originalName || `Photo ${currentIndex + 1}` }}</span>
          </div>
          <div class="info-item" v-if="images[currentIndex]?.fileSize">
            <span class="info-label">Size:</span>
            <span class="info-value">{{ formatFileSize(images[currentIndex].fileSize) }}</span>
          </div>
          <div class="info-item" v-if="images[currentIndex]?.createdAt">
            <span class="info-label">Uploaded:</span>
            <span class="info-value">{{ formatDateTime(images[currentIndex].createdAt) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { Camera, ZoomIn, ArrowLeft, ArrowRight } from '@element-plus/icons-vue';

export default {
  name: 'TaskCompletionGallery',
  components: {
    Camera,
    ZoomIn,
    ArrowLeft,
    ArrowRight,
  },
  props: {
    images: {
      type: Array,
      default: () => [],
    },
  },
  setup(props) {
    const fullscreenVisible = ref(false);
    const currentIndex = ref(0);

    // Methods
    const openFullscreen = (index) => {
      currentIndex.value = index;
      fullscreenVisible.value = true;
    };

    const nextImage = () => {
      if (currentIndex.value < props.images.length - 1) {
        currentIndex.value++;
      }
    };

    const previousImage = () => {
      if (currentIndex.value > 0) {
        currentIndex.value--;
      }
    };

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString('en-GB', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    };

    return {
      fullscreenVisible,
      currentIndex,
      openFullscreen,
      nextImage,
      previousImage,
      formatFileSize,
      formatDateTime,
    };
  },
};
</script>

<style scoped>
/* Gallery Container */
.task-completion-gallery {
  margin-top: 2rem;
}

.gallery-header {
  margin-bottom: 1.5rem;
}

.gallery-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.gallery-subtitle {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
  line-height: 1.4;
}

/* Image Grid */
.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.image-item {
  cursor: pointer;
  border-radius: 1rem;
  overflow: hidden;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 2px solid #e5e7eb;
}

.image-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
  border-color: #10b981;
}

.image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.completion-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-item:hover .completion-image {
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
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.view-btn {
  background: rgba(16, 185, 129, 0.9);
  border-color: #10b981;
  backdrop-filter: blur(4px);
}

.image-info {
  padding: 1rem;
  background: white;
  border-top: 1px solid #f3f4f6;
}

.image-name {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0.25rem;
}

.image-size {
  display: block;
  font-size: 0.75rem;
  color: #6b7280;
}

/* Fullscreen Dialog */
.fullscreen-dialog :deep(.el-dialog) {
  background: #000;
  border-radius: 0;
  margin: 0;
  max-width: 100vw;
  max-height: 100vh;
}

.fullscreen-dialog :deep(.el-dialog__header) {
  background: rgba(0, 0, 0, 0.8);
  color: white;
  backdrop-filter: blur(10px);
}

.fullscreen-dialog :deep(.el-dialog__body) {
  padding: 0;
  background: #000;
}

.fullscreen-gallery {
  position: relative;
  width: 100%;
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.gallery-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.fullscreen-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

/* Navigation */
.gallery-nav {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  pointer-events: none;
  z-index: 10;
}

.nav-btn {
  position: absolute;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  backdrop-filter: blur(10px);
  pointer-events: all;
}

.prev-btn {
  left: 2rem;
}

.next-btn {
  right: 2rem;
}

.nav-btn:hover {
  background: rgba(16, 185, 129, 0.8);
  color: white;
}

/* Thumbnail Strip */
.thumbnail-strip {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  overflow-x: auto;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s ease;
  flex-shrink: 0;
}

.thumbnail-item.active {
  border-color: #10b981;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Fullscreen Info */
.fullscreen-info {
  display: flex;
  gap: 2rem;
  justify-content: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  backdrop-filter: blur(10px);
}

.info-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.info-label {
  font-weight: 600;
  opacity: 0.7;
}

.info-value {
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 0.75rem;
  }

  .image-container {
    height: 150px;
  }

  .nav-btn {
    left: 1rem;
    right: 1rem;
  }

  .prev-btn {
    left: 1rem;
  }

  .next-btn {
    right: 1rem;
  }

  .fullscreen-info {
    flex-direction: column;
    gap: 0.5rem;
    align-items: center;
  }

  .thumbnail-strip {
    padding: 0.5rem;
    gap: 0.25rem;
  }

  .thumbnail-item {
    width: 40px;
    height: 40px;
  }
}

@media (max-width: 480px) {
  .images-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .gallery-title {
    font-size: 1.125rem;
  }
}
</style>