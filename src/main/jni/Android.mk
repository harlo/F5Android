LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog

LOCAL_MODULE    := F5Buffers
LOCAL_SRC_FILES := info_guardianproject_f5android_plugins_f5_F5Buffers.cpp


include $(BUILD_SHARED_LIBRARY)

