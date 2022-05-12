package minicp.util;

import com.github.guillaumederval.javagrading.Grade;

import java.io.File;
import java.io.FilePermission;
import java.security.PermissionCollection;
import java.security.Permissions;

public class DataPermissionFactory implements Grade.PermissionCollectionFactory {
    public DataPermissionFactory() {}

    @Override
    public PermissionCollection get() {
        PermissionCollection coll = new Permissions();
        coll.add(new FilePermission("<<ALL FILES>>", "read"));
        return coll;
    }
}
