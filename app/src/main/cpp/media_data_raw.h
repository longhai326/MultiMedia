//
// Created by longhai on 18-8-7.
//

#ifndef MEDIATEST_MEDIA_DATA_RAW_H
#define MEDIATEST_MEDIA_DATA_RAW_H

/**
 * Split Y, U, V planes in YUV420P file.
 * @param url  Location of Input YUV file.
 * @param w    Width of Input YUV file.
 * @param h    Height of Input YUV file.
 * @param num  Number of frames to process.
 *
 */
int yuv420_split(char *url, int w, int h, int num);

#endif //MEDIATEST_MEDIA_DATA_RAW_H
