<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100vh;
}

/* Global Quick Actions Alignment Fix */
.actions-list,
.actions-content,
.actions-grid {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  width: 100% !important;
  padding: 0.75rem !important;
}

.actions-list .el-button,
.actions-content .el-button,
.actions-grid .el-button {
  width: 100% !important;
  justify-content: center !important;
  text-align: center !important;
}

/* Ensure consistent button sizing across all roles */
.action-button,
.action-btn {
  width: 100% !important;
  justify-content: center !important;
  text-align: center !important;
  margin: 0 !important;
}
</style>
