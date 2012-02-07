package org.bimserver;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceImpl;

public class Converter {
	private EcorePackage ecorePackage = EcorePackage.eINSTANCE;
	private EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
	private EPackage ePackage;

	public static void main(String[] args) {
		new Converter().convert();
	}
	
	public void convert() {
		System.out.println("Starting");
		ePackage = ecoreFactory.createEPackage();
		ePackage.setName("ifc2x3");
		ePackage.setNsPrefix("iai");
		ePackage.setNsURI("http:///buildingsmart.ifc.ecore");

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
		XSDResourceImpl xsdSchemaResource = (XSDResourceImpl)resourceSet.getResource(URI.createFileURI("IFC2X3.xsd"), true);
		EList<EObject> contents = xsdSchemaResource.getContents();
		for (EObject eObject : contents) {
			if (eObject instanceof XSDSchema) {
				XSDSchema xsdSchema = (XSDSchema)eObject;
				
				
				
				for (XSDTypeDefinition xsdTypeDefinition : xsdSchema.getTypeDefinitions()) {
					if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition) {
						processSimpleType((XSDSimpleTypeDefinition)xsdTypeDefinition);
					} else if (xsdTypeDefinition instanceof XSDComplexTypeDefinition) {
						processComplexType((XSDComplexTypeDefinition)xsdTypeDefinition);
					}
				}
			}
		}
		
//		XSDEcoreBuilder xsdEcoreBuilder = new XSDEcoreBuilder();
//		Collection eCorePackages = xsdEcoreBuilder.generate(URI.createFileURI("IFC2X3.xsd"));
//
//		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
//		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
//		Resource resource = resourceSet.createResource(URI.createFileURI("ifc2x3.ecore"));
//
//		for (Iterator iter = eCorePackages.iterator(); iter.hasNext();) {
//			EPackage element = (EPackage) iter.next();
//			resource.getContents().add(element);
//		}
//
//		try {
//			resource.save(null);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		writeEMF("ifc2x3.ecore");
		System.out.println("Finished");
	}
	
	public void writeEMF(String fileName) {
		ResourceSet metaResourceSet = new ResourceSetImpl();
		metaResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMLResourceFactoryImpl());

		URI resUri = URI.createURI(fileName);
		Resource metaResource = metaResourceSet.createResource(resUri);
		metaResource.getContents().add(ePackage);
		try {
			metaResource.save(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processComplexType(XSDComplexTypeDefinition xsdTypeDefinition) {
		EClass eClass = ecoreFactory.createEClass();
		XSDParticle xsdParticle = (XSDParticle) xsdTypeDefinition.getContent();
		XSDModelGroup modelGroup = (XSDModelGroup) xsdParticle.getTerm();
		for (XSDParticle particle : modelGroup.getParticles()) {
			XSDTerm particleTerm = particle.getTerm();
		}
		eClass.setName(xsdTypeDefinition.getName());
		ePackage.getEClassifiers().add(eClass);
	}

	private void processSimpleType(XSDSimpleTypeDefinition xsdTypeDefinition) {
		if (!xsdTypeDefinition.getEnumerationFacets().isEmpty()) {
			EEnum eEnum = ecoreFactory.createEEnum();
			eEnum.setName(xsdTypeDefinition.getName());
			int i=0;
			for (XSDEnumerationFacet enumerationFacet : xsdTypeDefinition.getEnumerationFacets()) {
				EEnumLiteral eEnumLiteral = ecoreFactory.createEEnumLiteral();
				eEnumLiteral.setName(enumerationFacet.getLexicalValue());
				eEnumLiteral.setValue(i++);
				eEnum.getELiterals().add(eEnumLiteral);
			}
			ePackage.getEClassifiers().add(eEnum);
		}
	}
}