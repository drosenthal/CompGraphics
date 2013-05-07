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
 * MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
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

import net.java.games.jogl.*;
import java.util.*;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.lang.reflect.*;

/**
 * A utility object intended to be used by implementations to act as a cache
 * of which OpenGL functions are currently available on both the host machine
 * and display.
 */
public final class FunctionAvailabilityCache {

  FunctionAvailabilityCache(GLContext context)
  {
    this.context = context;
  }

  /**
   * Flush the cache. The cache will be rebuilt lazily as calls to {@link
   * #isFunctionAvailable(String)} are received.
   */
  public void flush()
  {
    availabilityCache.clear();
    availableExtensionCache.clear();
  }

  public boolean isFunctionAvailable(String glFunctionName)
  {
    //System.err.println("!!! CHECKING FOR AVAILABILITY OF: "+glFunctionName);

    Boolean available = (Boolean)availabilityCache.get(glFunctionName);

    if (available == null) // not in availabilityCache
    {
      if (isPartOfAvailableExtensions(glFunctionName) ||
          isPartOfGLCore(context.getGL().glGetString(GL.GL_VERSION), glFunctionName))
      {
        available = Boolean.TRUE;
      }
      else
      {
        available = Boolean.FALSE;
      }
      
      availabilityCache.put(glFunctionName, available);
    }

    return available.booleanValue();
  }

  public boolean isExtensionAvailable(String glExtensionName) {
    initAvailableExtensions();
    return availableExtensionCache.contains(glExtensionName);
  }

  protected void initAvailableExtensions() {
    // if hash is empty (meaning it was flushed), pre-cache it with the list
    // of extensions that are in the GL_EXTENSIONS string
    if (availableExtensionCache.isEmpty()) {
      GL gl = context.getGL();
      //System.err.println("!!! Pre-caching extension availability");
      String allAvailableExtensions =
        gl.glGetString(GL.GL_EXTENSIONS) + " " + context.getPlatformExtensionsString();
      StringTokenizer tok = new StringTokenizer(allAvailableExtensions);
      while (tok.hasMoreTokens()) {
        String availableExt = tok.nextToken().trim();
        availableExt = availableExt.intern();
        availableExtensionCache.add(availableExt);
        //System.err.println("!!!   Available: " + availableExt);
      }

      // put a dummy var in here so that the cache is no longer empty even if
      // no extensions are in the GL_EXTENSIONS string
      availableExtensionCache.add("<INTERNAL_DUMMY_PLACEHOLDER>");
    }
  }

  protected boolean isPartOfAvailableExtensions(String glFunctionName)
  {    
    initAvailableExtensions();

    // First, find the extension to which the function corresponds
    String extensionName = getExtensionCorrespondingToFunction(glFunctionName);

    // Now see if that extension is available
    boolean extensionAvailable = availableExtensionCache.contains(extensionName);

    return extensionAvailable;
  }

  /**
   * Returns true if the given OpenGL function is part of the OpenGL core
   * that corresponds to the give OpenGL version string.
   *
   * @param glVersionString must be of the form "X" or "X.Y" or "X.Y.Z", where
   * X, Y, and Z are integers
   * @exception GLException if the glFunctionName passed in is
   * not the name of any known OpenGL extension function. 
   */
  public static boolean isPartOfGLCore(String glVersionString, String glFunctionName)
  {
    String funcCoreVersionString =
      StaticGLInfo.getFunctionAssociation(glFunctionName);

    if (funcCoreVersionString == null) {
      // No extension string was found in the glext.h/wglext.h/glxext.h
      // headers when building the StaticGLInfo class. So either it's a new
      // extension that's not in those headers, or it's not an opengl
      // extension. Either way it's an illegal argument.
      throw new GLException(
        "Function \"" + glFunctionName + "\" does not " +
        "correspond to any known OpenGL extension or core version.");
    }

    Version actualVersion;
    try
    {
      actualVersion = new Version(funcCoreVersionString);
    }
    catch (IllegalArgumentException e)
    {      
      // funcCoreVersionString is not an OpenGL version identifier (i.e., not
      // of the form GL_VERSION_XXX).
      //
      // Since the association string returned from
      // StaticGLInfo.getFunctionAssociation() was not null, this function
      // must be an OpenGL extension function.
      //
      // Therefore this function can't be part of any OpenGL core.
      return false;
    }
    
    Version versionToCheck;
    try
    {
      versionToCheck = new Version(glVersionString);
    }
    catch (IllegalArgumentException e)
    {
      // user did not supply a valid OpenGL version identifier
      throw new IllegalArgumentException(
          "Illegally formatted OpenGL version identifier: \"" + glVersionString + "\"");
    }
    
    // See if the version number of glVersionString is less than or equal to
    // the OpenGL specification number to which the given function actually
    // belongs.
    if (actualVersion.compareTo(versionToCheck) <= 0)
    {
      System.err.println(
        glFunctionName + " is in core OpenGL " + glVersionString +
        " because it is in OpenGL " + funcCoreVersionString);
      return true;
    }
    
    System.err.println(
      glFunctionName + " is NOT a part of the OpenGL " + glVersionString + " core" +
      "; it is part of OpenGL " + funcCoreVersionString);

    return false;
  }

  /** Returns the extension name that corresponds to the given extension
   * function. For example, it will return "GL_EXT_vertex_array" when the
   * argument is "glNormalPointerEXT".
   *
   * Please see http://oss.sgi.com/projects/ogl-sample/registry/index.html for
   * a list of extension names and the functions they expose.
   */
  protected static String getExtensionCorrespondingToFunction(String glFunctionName)
  {
    // HACK: FIXME!!! return something I know is supported so I can test other
    // functions. 
    return StaticGLInfo.getFunctionAssociation(glFunctionName);
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private HashMap availabilityCache = new HashMap(50); 
  private HashSet availableExtensionCache = new HashSet(50);
  private GLContext context;

  /**
   * A class for storing and comparing revision version numbers.
   */
  private static class Version implements Comparable
  {
    private int major, minor, sub;
    public Version(int majorRev, int minorRev, int subMinorRev)
    {
      major = majorRev;
      minor = minorRev;
      sub = subMinorRev;
    }

    /**
     * @param versionString must be of the form "GL_VERSION_X" or
     * "GL_VERSION_X_Y" or "GL_VERSION_X_Y_Z", where X, Y, and Z are integers.
     *
     * @exception IllegalArgumentException if the argument is not a valid
     * OpenGL version identifier
     */
    public Version(String versionString)
    {
      if (! versionString.startsWith("GL_VERSION_"))
      {
        // not a version string
        throw new IllegalArgumentException(
          "Illegal version identifier \"" + versionString +
          "\"; does not start with \"GL_VERSION_\"");
      }
      
      try
      {
        StringTokenizer tok = new StringTokenizer(versionString, "_");

        tok.nextToken(); // GL_
        tok.nextToken(); // VERSION_ 
        if (!tok.hasMoreTokens()) { major = 0; return; }
        major = Integer.valueOf(tok.nextToken()).intValue();
        if (!tok.hasMoreTokens()) { minor = 0; return; }
        minor = Integer.valueOf(tok.nextToken()).intValue();
        if (!tok.hasMoreTokens()) { sub = 0; return; }
        sub = Integer.valueOf(tok.nextToken()).intValue();
      }
      catch (Exception e)
      {
        throw new IllegalArgumentException(
          "Illegally formatted version identifier: \"" + versionString + "\"");
      }
    }

    public int compareTo(Object o)
    {
      Version vo = (Version)o;
      if (major > vo.major) return 1; 
      else if (major < vo.major) return -1; 
      else if (minor > vo.minor) return 1; 
      else if (minor < vo.minor) return -1; 
      else if (sub > vo.sub) return 1; 
      else if (sub < vo.sub) return -1; 

      return 0; // they are equal
    }
    
  } // end class Version
}
