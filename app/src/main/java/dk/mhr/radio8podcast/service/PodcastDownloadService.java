package dk.mhr.radio8podcast.service;
/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static dk.mhr.radio8podcast.service.PodcastUtils.DOWNLOAD_NOTIFICATION_CHANNEL_ID;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.NotificationUtil;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadNotificationHelper;
import androidx.media3.exoplayer.offline.DownloadService;
import androidx.media3.exoplayer.scheduler.PlatformScheduler;
import androidx.media3.exoplayer.scheduler.Requirements;
import androidx.media3.exoplayer.scheduler.Scheduler;

//import com.google.android.exoplayer2.offline.Download;
//import com.google.android.exoplayer2.offline.DownloadManager;
//import com.google.android.exoplayer2.offline.DownloadService;
//import com.google.android.exoplayer2.scheduler.PlatformScheduler;
//import com.google.android.exoplayer2.scheduler.Requirements;
//import com.google.android.exoplayer2.scheduler.Scheduler;
//import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
//import com.google.android.exoplayer2.util.Log;
//import com.google.android.exoplayer2.util.NotificationUtil;
//import com.google.android.exoplayer2.util.Util;

import java.util.List;

import dk.mhr.radio8podcast.R;

/** A service for downloading media. */
public class PodcastDownloadService extends DownloadService {

    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    public PodcastDownloadService() {
        super(
                FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                R.string.notification_channelname,
                /* channelDescriptionResourceId= */ 0);

        Log.i("MHR", "Starting DemoDownloadService!!!");
    }

    @Override
    protected DownloadManager getDownloadManager() {
        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        DownloadManager downloadManager = PodcastUtils.getDownloadManager(/* context= */ this);
        DownloadNotificationHelper downloadNotificationHelper =
                PodcastUtils.getDownloadNotificationHelper(/* context= */ this);
        downloadManager.addListener(
                new TerminalStateNotificationHelper(
                        this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1));
        return downloadManager;
    }

    @Override
    protected Scheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(
            List<Download> downloads, @Requirements.RequirementFlags int notMetRequirements) {
        //return null;
        return PodcastUtils.getDownloadNotificationHelper(/* context= */ this)
                .buildProgressNotification(
                        /* context= */ this,
                        R.drawable.ic_download,
                        /* contentIntent= */ null,
                        /* message= */ null,
                        downloads,
                        notMetRequirements);
    }

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     *
     * <p>This helper will outlive the lifespan of a single instance of {@link PodcastDownloadService}.
     * It is static to avoid leaking the first {@link PodcastDownloadService} instance.
     */
    private static final class TerminalStateNotificationHelper implements DownloadManager.Listener {

        private final Context context;
        private final DownloadNotificationHelper notificationHelper;

        private int nextNotificationId;

        public TerminalStateNotificationHelper(
                Context context, DownloadNotificationHelper notificationHelper, int firstNotificationId) {
            this.context = context.getApplicationContext();
            this.notificationHelper = notificationHelper;
            nextNotificationId = firstNotificationId;
        }

        @Override
        public void onDownloadChanged(
                DownloadManager downloadManager, Download download, @Nullable Exception finalException) {
            Notification notification;

            Log.i("MHR", "onDownloadChanged called: " + download.state);

            if (download.state == Download.STATE_COMPLETED) {
                notification =
                        notificationHelper.buildDownloadCompletedNotification(
                                context,
                                R.drawable.ic_download_done,
                                /* contentIntent= */ null,
                                Util.fromUtf8Bytes(download.request.data));
            } else if (download.state == Download.STATE_FAILED) {
                notification =
                        notificationHelper.buildDownloadFailedNotification(
                                context,
                                R.drawable.ic_download_done,
                                /* contentIntent= */ null,
                                Util.fromUtf8Bytes(download.request.data));
            } else {
                return;
            }
            NotificationUtil.setNotification(context, nextNotificationId++, notification);
        }
    }
}