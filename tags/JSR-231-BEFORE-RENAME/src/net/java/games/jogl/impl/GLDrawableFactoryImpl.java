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

package net.java.games.jogl.impl;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import net.java.games.gluegen.runtime.*; // for PROCADDRESS_VAR_PREFIX
import net.java.games.jogl.*;

/** Extends GLDrawableFactory with a few methods for handling
    typically software-accelerated offscreen rendering (Device
    Independent Bitmaps on Windows, pixmaps on X11). Direct access to
    these GLDrawables is not supplied directly to end users, though
    they may be instantiated by the GLJPanel implementation. */
public abstract class GLDrawableFactoryImpl extends GLDrawableFactory {
  /** Creates a (typically software-accelerated) offscreen GLDrawable
      used to implement the fallback rendering path of the
      GLJPanel. */
  public abstract GLDrawableImpl createOffscreenDrawable(GLCapabilities capabilities,
                                                         GLCapabilitiesChooser chooser);

  /** Helper routine which resets a ProcAddressTable generated by the
      GLEmitter by looking up anew all of its function pointers. */
  public void resetProcAddressTable(Object table) {
    Class tableClass = table.getClass();
    java.lang.reflect.Field[] fields = tableClass.getDeclaredFields();
    
    for (int i = 0; i < fields.length; ++i) {
      String addressFieldName = fields[i].getName();
      if (!addressFieldName.startsWith(ProcAddressHelper.PROCADDRESS_VAR_PREFIX)) {
        // not a proc address variable
        continue;
      }
      int startOfMethodName = ProcAddressHelper.PROCADDRESS_VAR_PREFIX.length();
      String glFuncName = addressFieldName.substring(startOfMethodName);
      try {
        java.lang.reflect.Field addressField = tableClass.getDeclaredField(addressFieldName);
        assert(addressField.getType() == Long.TYPE);
        long newProcAddress = dynamicLookupFunction(glFuncName);
        // set the current value of the proc address variable in the table object
        addressField.setLong(table, newProcAddress); 
      } catch (Exception e) {
        throw new GLException("Cannot get GL proc address for method \"" +
                              glFuncName + "\": Couldn't set value of field \"" + addressFieldName +
                              "\" in class " + tableClass.getName(), e);
      }
    }
  }

  /** Dynamically looks up the given function. */
  public abstract long dynamicLookupFunction(String glFuncName);

  public static GLDrawableFactoryImpl getFactoryImpl() {
    return (GLDrawableFactoryImpl) getFactory();
  }
}
