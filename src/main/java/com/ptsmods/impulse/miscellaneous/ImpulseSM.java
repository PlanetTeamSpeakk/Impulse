package com.ptsmods.impulse.miscellaneous;

import java.security.Permission;
import java.util.PropertyPermission;

public class ImpulseSM extends SecurityManager {

	@Override
	public void checkPermission(Permission perm) {
		if (perm instanceof RuntimePermission && perm.getName().equals("setSecurityManager"))
			throw new SecurityException("Cannot override Impulse SecurityManager.");
		else if (perm instanceof PropertyPermission && perm.getName().equals("user.language") && perm.getActions().equals("write"))
			throw new SecurityException("Cannot override default Locale.");
		else if (perm instanceof PropertyPermission && perm.getName().equals("user.timezone") && perm.getActions().equals("write")) throw new SecurityException("Cannot override default TimeZone.");
	}

	@Override
	public void checkDelete(String file) {
		if (file.endsWith("config.cfg")) throw new SecurityException("Cannot delete config.cfg.");
	}

}
