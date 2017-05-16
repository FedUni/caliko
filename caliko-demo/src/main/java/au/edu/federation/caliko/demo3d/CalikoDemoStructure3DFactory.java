package au.edu.federation.caliko.demo3d;

/**
 * @author jsalvo
 */
public final class CalikoDemoStructure3DFactory {
	
	public static CalikoDemoStructure3D makeDemoStructure3D(int demoNumber) throws Exception {
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
	
	private enum CalikoDemoStructure3DEnum {
		UNCONSTRAINED_BONES(UnconstrainedBones.class, 1),
		ROTORBALL_CONSTRAINED_BONES(RotorBallJointConstrainedBones.class, 2),
		ROTOR_CONSTRAINED_BASEBONES(RotorConstrainedBaseBones.class, 3),
		FREELY_ROTATING_GLOBAL_HINGES(FreelyRotatingGlobalHinges.class, 4),
		GLOBAL_HINGES_WITH_REFERENCE_AXIS_CONSTRAINTS(GlobalHingesWithReferenceAxisConstraints.class, 5),
		FREELY_ROTATING_LOCAL_HINGES(FreelyRotatingLocalHinges.class, 6),
		LOCAL_HINGES_WITH_REFERENCE_AXIS_CONSTRAINTS(LocalHingesWithReferenceAxisConstraints.class, 7),
		CONNECTED_CHAINS(ConnectedChains.class, 8),
		GLOBAL_ROTOR_CONSTRAINED_CONNECTED_CHAINS(GlobalRotorConstrainedConnectedChains.class, 9),
		LOCAL_ROTOR_CONSTRAINED_CONNECTED_CHAINS(LocalRotorConstrainedConnectedChains.class, 10),
		CONNECTED_CHAINS_WITH_FREELY_ROTATING_GLOBAL_HINGES_BASEBONE_CONSTRAINTS(ConnectedChainsWithFreelyRotatingGlobalHingesBaseboneConstraints.class, 11),
		CONNECTED_CHAINS_WITH_EMBEDDED_TARGETS(ConnectedChainsWithEmbeddedTargets.class, 12);
		
		
		Class<? extends CalikoDemoStructure3D> clazz;
		int demoNumber;
		
		private <T extends CalikoDemoStructure3D> CalikoDemoStructure3DEnum(Class<T> clazz, int demoNumber) {
			this.clazz = clazz;
			this.demoNumber = demoNumber;
		}
		
		public Class<? extends CalikoDemoStructure3D> getClazz() {
			return this.clazz;
		}
		
		public int getDemoNumber() {
			return this.demoNumber;
		}
	}

}
