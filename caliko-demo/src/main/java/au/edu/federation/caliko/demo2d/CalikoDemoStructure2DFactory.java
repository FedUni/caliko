package au.edu.federation.caliko.demo2d;

/**
 * @author jsalvo
 */
public class CalikoDemoStructure2DFactory {
	
	public static CalikoDemoStructure2D makeDemoStructure2D(int demoNumber) throws ReflectiveOperationException {
		CalikoDemoStructure2DEnum aDemo = findEnumForDemoNumber(demoNumber);
		if(aDemo != null) {
			return aDemo.getClazz().newInstance();
		}
		return null;
	}
	
	private static CalikoDemoStructure2DEnum findEnumForDemoNumber(int demoNumber) {
		for(CalikoDemoStructure2DEnum aDemo : CalikoDemoStructure2DEnum.values()) {
			if(aDemo.getDemoNumber() == demoNumber) {
				return aDemo;
			}
		}
		return null;
	}	
	
	public enum CalikoDemoStructure2DEnum {
		
		FIXEDBASE_ABSOLUTEBASEBONEJOINT_CONSTRAINTS(FixedBaseAbsoluteBaseBoneJointConstraints.class),
		FIXEDBASE_UNCONSTRAINEDBASEBONE_UNCONSTRAINEDBONES(FixedBaseUnconstrainedBaseBaseMultipleUnconstrainedBones.class),
		FIXEDBASE_UNCONSTRAINEDBASEBONE_CONSTRAINEDBONES(FixedBaseUnconstrainedBaseBoneMultipleConstrainedBones.class),
		MULTIPLECONNECTEDCHAINS_NOBASEBONE_CONSTRAINTS(MultipleConnectedChainsNoBaseBoneConstraints.class),
		MULTIPLECONNECTEDCHAINS_LOCALRELATIVEBASEBONE_CONSTRAINTS(MultipleConnectedChainsLocalRelativeBaseBoneConstraints.class),
		MULTIPLECONNECTEDCHAINS_LOCALABSOLUTEBASEBONE_CONSTRAINTS(MultipleConnectedChainsLocalAbsoluteBaseBoneConstraints.class),
		VARYINGOFFSET_FIXEDCHAINS_WITHEMBEDDED_TARGETS(VaryingOffsetFixedChainsWithEmbeddedTargets.class),
		MULTIPLENESTEDCHAINS_SEMIRANDOM(MultipleNestedChainsSemiRandom.class),
		WORLDSPACE_BONE_CONSTRAINTS(WorldSpaceBoneConstraints.class);		
		
		Class<? extends CalikoDemoStructure2D> clazz;
		
		private <T extends CalikoDemoStructure2D> CalikoDemoStructure2DEnum(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		public Class<? extends CalikoDemoStructure2D> getClazz() {
			return this.clazz;
		}
		
		public int getDemoNumber() {
			return this.ordinal() + 1;
		}
		
	}

}
