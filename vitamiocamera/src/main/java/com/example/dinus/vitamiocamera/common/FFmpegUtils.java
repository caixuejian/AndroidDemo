package com.example.dinus.vitamiocamera.common;

import com.yixia.videoeditor.adapter.UtilityAdapter;

public enum  FFmpegUtils {
    INSTANCE;

    public static boolean captureThumbnails(String videoPath, String outputPath, String wh, String ss) {
        com.yixia.weibo.sdk.util.FileUtils.deleteFile(outputPath);
        if(ss == null) {
            ss = "";
        } else {
            ss = "-ss " + ss + " -t 0.001";
        }

        //#ffmpeg -i test.avi -y -f image2 -ss 8 -t 0.001 -s 350x240 test.jpg

        String cmd = String.format("ffmpeg -i \"%s\" -y -f image2 -r 30 %s -s %s \"%s\"", new Object[]{videoPath, ss, wh, outputPath});
        return UtilityAdapter.FFmpegRun("", cmd) == 0;
    }
}
