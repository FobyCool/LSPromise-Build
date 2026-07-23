package org.lsposed.lspromise;

public class DirtyFrag {

    // Step 1: patch vendor file as kernel module
    public static native int patchMod();

    // Step 2: patch libc for modprobe to load module
    public static native int patchLibc();

    // Step 3: patch libc++ for init to execute modprobe
    public static native int patchCxx();

    // Step 4: create orphaned process and kill it to trigger init shell code
    public static native int createOrphanProcess();
}
