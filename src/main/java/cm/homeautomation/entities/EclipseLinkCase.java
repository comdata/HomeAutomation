package cm.homeautomation.entities;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class EclipseLinkCase extends PhysicalNamingStrategyStandardImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7891715509384073398L;

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		
		return new Identifier(name.getText().toUpperCase(), true);
	}
	
	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return new Identifier(name.getText().toUpperCase(), true);
	}
	
}
