/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package net.java.games.jogl.impl.x11;

import java.awt.Component;

import net.java.games.jogl.*;
import net.java.games.jogl.impl.*;

public class X11OnscreenGLDrawable extends X11GLDrawable {
  public static final int LOCK_SURFACE_NOT_READY = 1;
  public static final int LOCK_SURFACE_CHANGED = 2;
  public static final int LOCK_SUCCESS = 3;

  // Variables for lockSurface/unlockSurface
  private JAWT_DrawingSurface ds;
  private JAWT_DrawingSurfaceInfo dsi;
  private JAWT_X11DrawingSurfaceInfo x11dsi;
  
  // Indicates whether the component (if an onscreen context) has been
  // realized. Plausibly, before the component is realized the JAWT
  // should return an error or NULL object from some of its
  // operations; this appears to be the case on Win32 but is not true
  // at least with Sun's current X11 implementation (1.4.x), which
  // crashes with no other error reported if the DrawingSurfaceInfo is
  // fetched from a locked DrawingSurface during the validation as a
  // result of calling show() on the main thread. To work around this
  // we prevent any JAWT or OpenGL operations from being done until
  // addNotify() is called on the component.
  protected boolean realized;

  public X11OnscreenGLDrawable(Component component) {
    super(component, null, null);
  }

  public GLContext createContext(GLContext shareWith) {
    return new X11OnscreenGLContext(this, shareWith);
  }

  public void setRealized(boolean realized) {
    this.realized = realized;
  }

  public void setSize(int width, int height) {
    component.setSize(width, height);
  }

  public int getWidth() {
    return component.getWidth();
  }

  public int getHeight() {
    return component.getHeight();
  }

  public void swapBuffers() throws GLException {
    lockAWT();
    try {    
      boolean didLock = false;
      
      if (drawable == 0) {
        if (lockSurface() == LOCK_SURFACE_NOT_READY) {
          return;
        }

        didLock = true;
      }

      GLX.glXSwapBuffers(display, drawable);

      if (didLock) {
        unlockSurface();
      }
    } finally {
    unlockAWT();
    }
  }

  public int lockSurface() throws GLException {
    if (!realized) {
      return LOCK_SURFACE_NOT_READY;
    }
    if (drawable != 0) {
      throw new GLException("Surface already locked");
    }
    ds = getJAWT().GetDrawingSurface(component);
    if (ds == null) {
      // Widget not yet realized
      return LOCK_SURFACE_NOT_READY;
    }
    int res = ds.Lock();
    if ((res & JAWTFactory.JAWT_LOCK_ERROR) != 0) {
      throw new GLException("Unable to lock surface");
    }
    // See whether the surface changed and if so destroy the old
    // OpenGL context so it will be recreated (NOTE: removeNotify
    // should handle this case, but it may be possible that race
    // conditions can cause this code to be triggered -- should test
    // more)
    int ret = LOCK_SUCCESS;
    if ((res & JAWTFactory.JAWT_LOCK_SURFACE_CHANGED) != 0) {
      ret = LOCK_SURFACE_CHANGED;
    }
    dsi = ds.GetDrawingSurfaceInfo();
    if (dsi == null) {
      // Widget not yet realized
      ds.Unlock();
      getJAWT().FreeDrawingSurface(ds);
      ds = null;
      return LOCK_SURFACE_NOT_READY;
    }
    x11dsi = (JAWT_X11DrawingSurfaceInfo) dsi.platformInfo();
    display = x11dsi.display();
    drawable = x11dsi.drawable();
    visualID = x11dsi.visualID();
    if (display == 0 || drawable == 0) {
      // Widget not yet realized
      ds.FreeDrawingSurfaceInfo(dsi);
      ds.Unlock();
      getJAWT().FreeDrawingSurface(ds);
      ds = null;
      dsi = null;
      x11dsi = null;
      display = 0;
      drawable = 0;
      visualID = 0;
      return LOCK_SURFACE_NOT_READY;
    }
    return ret;
  }

  public void unlockSurface() {
    if (drawable == 0) {
      throw new GLException("Surface already unlocked");
    }
    ds.FreeDrawingSurfaceInfo(dsi);
    ds.Unlock();
    getJAWT().FreeDrawingSurface(ds);
    ds = null;
    dsi = null;
    x11dsi = null;
    display = 0;
    drawable = 0;
    visualID = 0;
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private JAWT getJAWT() {
    return X11GLDrawableFactory.getJAWT();
  }
}
