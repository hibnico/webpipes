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
package org.hibnet.webpipes.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hibnet.webpipes.Webpipe;

public class ResourceRefresher {

    private volatile long sleepTime = 1000;

    private List<Webpipe> webpipes = new CopyOnWriteArrayList<>();

    private List<Resource> resources = new CopyOnWriteArrayList<>();

    private volatile boolean sleepTimeChanged = false;

    private volatile boolean interuptable = false;

    private Thread thread;

    private volatile boolean stop = false;

    public void setSleepTime(long sleepTime) {
        synchronized (this) {
            this.sleepTime = sleepTime;
            sleepTimeChanged = true;
            if (thread != null && interuptable) {
                thread.interrupt();
            }
        }
    }

    public void setWebpipes(Collection< ? extends Webpipe> webpipes) {
        this.webpipes = new CopyOnWriteArrayList<>(webpipes);
    }

    public void addWebpipe(Webpipe webpipe) {
        webpipes.add(webpipe);
    }

    public void removeWebpipe(Webpipe webpipe) {
        webpipes.remove(webpipe);
    }

    public void setResources(Collection< ? extends Resource> resources) {
        this.resources = new CopyOnWriteArrayList<>(resources);
    }

    public void addResource(Resource resource) {
        resources.add(resource);
    }

    public void removeResource(Resource resource) {
        resources.remove(resource);
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
                    } catch (IOException e) {
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
                synchronized (ResourceRefresher.this) {
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
                            synchronized (ResourceRefresher.this) {
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
        thread.setName("WRO Resource Refresher");
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

    public void refreshAll() throws IOException {
        List<Webpipe> webpipesToRefresh = new ArrayList<>();
        Map<Resource, Boolean> changedResources = new HashMap<>();

        // find resources that have changed
        for (Resource r : resources) {
            Boolean changedResource = changedResources.get(r);
            if (changedResource == null) {
                changedResource = r.refresh();
                changedResources.put(r, changedResource);
            }
        }

        // find webpipes which at least one resource has changed
        for (Webpipe webpipe : webpipes) {
            boolean changed = false;
            for (Resource r : webpipe.getResources()) {
                // we expect every resource of the webpipe to have been checked in
                changed = changedResources.get(r);
                if (changed) {
                    break;
                }
            }
            if (changed) {
                webpipe.invalidateCachedContent();
                webpipesToRefresh.add(webpipe);
            }
        }

        // force the regeneration
        for (Entry<Resource, Boolean> resourceStatus : changedResources.entrySet()) {
            if (resourceStatus.getValue()) {
                resourceStatus.getKey().getContent();
            }
        }
        for (Webpipe webpipe : webpipesToRefresh) {
            webpipe.getContents();
        }
    }

}
