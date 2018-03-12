package cm.homeautomation.odata;

import javax.persistence.EntityManagerFactory;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OError;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class JPAOdataProducer {

	protected static void report(String msg) {
		System.out.println(msg);
	}

	protected static void report(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	protected static int reportEntities(ODataConsumer c, String entitySetHref, int limit) {
		report("entitySetHref: " + entitySetHref);
		final Enumerable<OEntity> entities = c.getEntities(entitySetHref).execute().take(limit);
		return reportEntities(entitySetHref, entities);
	}

	protected static int reportEntities(String entitySetHref, Enumerable<OEntity> entities) {

		int count = 0;

		for (final OEntity e : entities) {
			reportEntity(entitySetHref + " entity" + count, e);
			count++;
		}
		report("total count: %s \n\n", count);

		return count;
	}

	protected static void reportEntity(String caption, OEntity entity) {
		report(caption);
		if (entity.getEntityTag() != null) {
			report("  ETag: %s", entity.getEntityTag());
		}
		for (final OProperty<?> p : entity.getProperties()) {
			Object v = p.getValue();
			if (p.getType().equals(EdmSimpleType.BINARY) && (v != null)) {
				v = org.odata4j.repack.org.apache.commons.codec.binary.Base64.encodeBase64String((byte[]) v).trim();
			}
			report("  %s: %s", p.getName(), v);
		}
	}

	protected static void reportError(OError error) {
		report("Error code=%s", error.getCode());
		report("Error message=%s", error.getMessage());
		if (error.getInnerError() != null) {
			report("Inner error=%s", error.getInnerError());
		}
	}

	protected static void reportMetadata(EdmDataServices services) {

		for (final EdmSchema schema : services.getSchemas()) {
			report("Schema Namespace=%s, Alias=%s", schema.getNamespace(), schema.getAlias());

			for (final EdmEntityType et : schema.getEntityTypes()) {
				String ets = String.format("  EntityType Name=%s", et.getName());
				if (et.getHasStream() != null) {
					ets = ets + " HasStream=" + et.getHasStream();
				}
				report(ets);

				for (final String key : et.getKeys()) {
					report("    Key PropertyRef Name=%s", key);
				}

				reportProperties(et.getDeclaredProperties());
				for (final EdmNavigationProperty np : et.getDeclaredNavigationProperties()) {
					report("    NavigationProperty Name=%s Relationship=%s FromRole=%s ToRole=%s", np.getName(),
							np.getRelationship().getFQNamespaceName(), np.getFromRole().getRole(),
							np.getToRole().getRole());
				}

			}
			for (final EdmComplexType ct : schema.getComplexTypes()) {
				report("  ComplexType Name=%s", ct.getName());

				reportProperties(ct.getProperties());

			}
			for (final EdmAssociation assoc : schema.getAssociations()) {
				report("  Association Name=%s", assoc.getName());
				report("    End Role=%s Type=%s Multiplicity=%s", assoc.getEnd1().getRole(),
						assoc.getEnd1().getType().getFullyQualifiedTypeName(), assoc.getEnd1().getMultiplicity());
				report("    End Role=%s Type=%s Multiplicity=%s", assoc.getEnd2().getRole(),
						assoc.getEnd2().getType().getFullyQualifiedTypeName(), assoc.getEnd2().getMultiplicity());
			}
			for (final EdmEntityContainer ec : schema.getEntityContainers()) {
				report("  EntityContainer Name=%s IsDefault=%s LazyLoadingEnabled=%s", ec.getName(), ec.isDefault(),
						ec.getLazyLoadingEnabled());

				for (final EdmEntitySet ees : ec.getEntitySets()) {
					report("    EntitySet Name=%s EntityType=%s", ees.getName(),
							ees.getType().getFullyQualifiedTypeName());
				}

				for (final EdmAssociationSet eas : ec.getAssociationSets()) {
					report("    AssociationSet Name=%s Association=%s", eas.getName(),
							eas.getAssociation().getFQNamespaceName());
					report("      End Role=%s EntitySet=%s", eas.getEnd1().getRole().getRole(),
							eas.getEnd1().getEntitySet().getName());
					report("      End Role=%s EntitySet=%s", eas.getEnd2().getRole().getRole(),
							eas.getEnd2().getEntitySet().getName());
				}

				for (final EdmFunctionImport efi : ec.getFunctionImports()) {
					report("    FunctionImport Name=%s EntitySet=%s ReturnType=%s HttpMethod=%s", efi.getName(),
							efi.getEntitySet() == null ? null : efi.getEntitySet().getName(), efi.getReturnType(),
							efi.getHttpMethod());
					for (final EdmFunctionParameter efp : efi.getParameters()) {
						report("      Parameter Name=%s Type=%s Mode=%s", efp.getName(), efp.getType(), efp.getMode());
					}
				}
			}
		}
	}

	private static void reportProperties(Iterable<EdmProperty> properties) {
		for (final EdmProperty property : properties) {
			String p = String.format("Property Name=%s Type=%s Nullable=%s", property.getName(), property.getType(),
					property.isNullable());
			if (property.getMaxLength() != null) {
				p = p + " MaxLength=" + property.getMaxLength();
			}
			if (property.getUnicode() != null) {
				p = p + " Unicode=" + property.getUnicode();
			}
			if (property.getFixedLength() != null) {
				p = p + " FixedLength=" + property.getFixedLength();
			}

			if (property.getStoreGeneratedPattern() != null) {
				p = p + " StoreGeneratedPattern=" + property.getStoreGeneratedPattern();
			}
			if (property.getConcurrencyMode() != null) {
				p = p + " ConcurrencyMode=" + property.getConcurrencyMode();
			}

			if (property.getFcTargetPath() != null) {
				p = p + " TargetPath=" + property.getFcTargetPath();
			}
			if (property.getFcContentKind() != null) {
				p = p + " ContentKind=" + property.getFcContentKind();
			}
			if (property.getFcKeepInContent() != null) {
				p = p + " KeepInContent=" + property.getFcKeepInContent();
			}
			if (property.getFcContentKind() != null) {
				p = p + " EpmContentKind=" + property.getFcContentKind();
			}
			if (property.getFcEpmKeepInContent() != null) {
				p = p + " EpmKeepInContent=" + property.getFcEpmKeepInContent();
			}
			report("    " + p);
		}
	}

	public JPAOdataProducer() {
		runODataProducer();
	}

	private void runODataProducer() {
		final String endpointUri = "http://localhost:8080/HomeAuotmationJpaProducer.svc/";

		// this example assumes you have an appropriate persistence.xml containing a
		// valid persistence unit definition
		// (in this case named NorthwindServiceEclipseLink) mapping your jpa entity
		// classes, etc

		// create a JPAProducer by giving it a EntityManagerFactory

		final EntityManagerFactory emf = EntityManagerService.getManager().getEntityManagerFactory();

		final JPAProducer producer = new JPAProducer(emf, "HA", 50);

		// register the producer as the static instance, then launch the http server
		DefaultODataProducerProvider.setInstance(producer);
		new ODataServerFactory(JaxRsImplementation.JERSEY).hostODataServer(endpointUri);

	}
}
