package com.qcloud.cos.meta;

public enum ListPattern {
	DIR_ONLY("eListDirOnly"),
	FILE_ONLY("eListFileOnly"),
	BOTH("eListBoth");

    private String pattern;

    private ListPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return this.pattern;
    }
}
