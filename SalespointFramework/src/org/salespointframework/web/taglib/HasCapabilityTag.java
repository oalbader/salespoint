package org.salespointframework.web.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.salespointframework.core.shop.Shop;
import org.salespointframework.core.user.PersistentUser;
import org.salespointframework.core.user.PersistentUserManager;
import org.salespointframework.core.user.TransientUser;
import org.salespointframework.core.user.TransientUserManager;
import org.salespointframework.core.user.User;
import org.salespointframework.core.user.Capability;
import org.salespointframework.core.user.UserManager;

/**
 * 
 * @author Lars Kreisz
 * @author Uwe Schmidt
 * @author Paul Henke
 */
@SuppressWarnings("serial")
public class HasCapabilityTag extends BodyTagSupport
{
	private String capabilityName;
	private boolean test = true;

	public void setCapabilityName(String capabilityName)
	{
		this.capabilityName = capabilityName;
	}
	
	/**
	 * The test condition that determines whether or not the body content should be processed.
	 * @param test a boolean condition
	 */
	public void setTest(boolean test)
	{
		this.test = test;
	}


	@Override
	public int doStartTag() throws JspException
	{
		@SuppressWarnings("unchecked")
		UserManager<?> temp = Shop.INSTANCE.getUserManager();
		
		if(temp == null) {
			throw new NullPointerException("Shop.INSTANCE.getUserManager() returned null");
		}
		
		UserManager<User> usermanager = (UserManager<User>) temp;
		
		User user = null;
		
		if(temp instanceof TransientUserManager) {
			user = usermanager.getUserByToken(TransientUser.class, pageContext.getSession());
		}
		
		if(temp instanceof PersistentUserManager) {
			user = usermanager.getUserByToken(PersistentUser.class, pageContext.getSession());
		}
		
		if(!(temp instanceof TransientUserManager && temp instanceof PersistentUserManager)) {
			user = usermanager.getUserByToken(User.class, pageContext.getSession());
		}
		
		Capability capability = new Capability(capabilityName);
		
			
		if (user != null)
		{
			boolean hasCapability = user.hasCapability(capability);
			
			if(hasCapability && test) {
				return EVAL_BODY_INCLUDE;
			} 
			
			if(!hasCapability && !test) {
				return EVAL_BODY_INCLUDE;
			}
		} else {
			if(!test) {
				return EVAL_BODY_INCLUDE;
			}
		}
		
		return SKIP_BODY;
	}
}
 