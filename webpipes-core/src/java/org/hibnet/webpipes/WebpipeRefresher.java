/*
 *  Copyright 2014-2015 WebPipes contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.webpipes;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebpipeRefresher {

    private static final Logger logger = LoggerFactory.getLogger(WebpipeRefresher.class);

    private volatile long sleepTime = 1000;

    private WebpipesBuilder webpipesBuilder;

    private volatile boolean sleepTimeChanged = false;

    private volatile boolean interuptable = false;

    private Thread thread;

    private volatile boolean stop = false;

    private boolean refreshDefinitions = true;

    public void setSleepTime(long sleepTime) {
        synchronized (this) {
            this.sleepTime = sleepTime;
            sleepTimeChanged = true;
            if (thread != null && interuptable) {
                thread.interrupt();
            }
        }
    }

    public WebpipeRefresher(WebpipesBuilder webpipesBuilder) {
        this.webpipesBuilder = webpipesBuilder;
    }

    public void setRefreshDefinitions(boolean refreshDefinitions) {
        this.refreshDefinitions = refreshDefinitions;
    }

    public synchronized void startWatcher() {
        if (thread != null) {
            return;
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        refreshAll();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    boolean quit = sleep();
                    if (quit) {
                        return;
                    }
                }
            }

            private boolean sleep() {
                synchronized (WebpipeRefresher.this) {
                    if (stop) {
                        stop = false;
                        return true;
                    }
                }
                long time = System.currentTimeMillis();
                long sleep = sleepTime;
                interuptable = true;
                try {
                    while (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                            return false;
                        } catch (InterruptedException e) {
                            synchronized (WebpipeRefresher.this) {
                                if (sleepTimeChanged) {
                                    // interrupted so the sleep time can actually changed
                                    // compute the remaining time to sleep according to what has been already done what what is newly expected
                                    sleep = sleepTime - (time - System.currentTimeMillis());
                                    sleepTimeChanged = false;
                                } else if (stop) {
                                    stop = false;
                                    return true;
                                } else {
                                    // the application is quitting
                                    return true;
                                }
                            }
                        }
                    }
                } finally {
                    interuptable = false;
                }
                return false;
            }
        });
        thread.setName("Webpipes Refresher");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stopWatcher() {
        if (thread == null) {
            return;
        }
        stop = true;
        if (interuptable) {
            thread.interrupt();
        }
        thread = null;
    }

    public void refreshAll() throws Exception {
        if (refreshDefinitions) {
            webpipesBuilder.refreshedWebpipes();
        }
        List<Webpipe> webpipesRefreshed = new ArrayList<>();
        for (Webpipe webpipe : webpipesBuilder.getWebpipes().values()) {
            if (webpipe.refresh()) {
                webpipesRefreshed.add(webpipe);
            }
        }
        for (Webpipe webpipe : webpipesRefreshed) {
            try {
                webpipe.getOutput();
            } catch (Exception e) {
                logger.error("Unable to get the content of the webpipe '" + webpipe.getPath() + "'", e);
            }
        }
    }

}
