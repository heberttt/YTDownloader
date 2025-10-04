package com.hebert.hdownloader.Enum;

public enum ThumbnailQuality {
    LOW {
        @Override
        public String toString() {
            return "low";
        }
    },
    MEDIUM {
        @Override
        public String toString() {
            return "medium";
        }
    },
    HIGH {
        @Override
        public String toString() {
            return "high";
        }
    },
}
