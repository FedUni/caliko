package au.edu.federation.caliko.demo3d;

/**
 * @author jsalvo
 */
public final class CalikoDemoStructure3DFactory {
	
	public static CalikoDemoStructure3D makeDemoStructure3D(int demoNumber) throws ReflectiveOperationException {
		CalikoDemoStructure3DEnum aDemo = findEnumForDemoNumber(demoNumber);
		if(aDemo !=null ) {
			return aDemo.getClazz().newInstance();
		}
		return null;
	}
	
	private static CalikoDemoStructure3DEnum findEnumForDemoNumber(int demoNumber) {
		for(CalikoDemoStructure3DEnum aDemo : CalikoDemoStructure3DEnum.values()) {
			if(aDemo.getDemoNumber() == demoNumber) {
				return aDemo;
			}
		}
		return null;
	}
	
	public enum CalikoDemoStructure3DEnum {
		UNCONSTRAINED_BONES(UnconstrainedBones.class),
		ROTORBALL_CONSTRAINED_BONES(RotorBallJointConstrainedBones.class),
		ROTOR_CONSTRAINED_BASEBONES(RotorConstrainedBaseBones.class),
		FREELY_ROTATING_GLOBAL_HINGES(FreelyRotatingGlobalHinges.class),
		GLOBAL_HINGES_WITH_REFERENCE_AXIS_CONSTRAINTS(GlobalHingesWithReferenceAxisConstraints.class),
		FREELY_ROTATING_LOCAL_HINGES(FreelyRotatingLocalHinges.class),
		LOCAL_HINGES_WITH_REFERENCE_AXIS_CONSTRAINTS(LocalHingesWithReferenceAxisConstraints.class),
		CONNECTED_CHAINS(ConnectedChains.class),
		GLOBAL_ROTOR_CONSTRAINED_CONNECTED_CHAINS(GlobalRotorConstrainedConnectedChains.class),
		LOCAL_ROTOR_CONSTRAINED_CONNECTED_CHAINS(LocalRotorConstrainedConnectedChains.class),
		CONNECTED_CHAINS_WITH_FREELY_ROTATING_GLOBAL_HINGES_BASEBONE_CONSTRAINTS(ConnectedChainsWithFreelyRotatingGlobalHingesBaseboneConstraints.class),
		CONNECTED_CHAINS_WITH_EMBEDDED_TARGETS(ConnectedChainsWithEmbeddedTargets.class);
		
		
		Class<? extends CalikoDemoStructure3D> clazz;
		
		private <T extends CalikoDemoStructure3D> CalikoDemoStructure3DEnum(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		public Class<? extends CalikoDemoStructure3D> getClazz() {
			return this.clazz;
		}
		
		public int getDemoNumber() {
			return this.ordinal() + 1;
		}
	}

}
