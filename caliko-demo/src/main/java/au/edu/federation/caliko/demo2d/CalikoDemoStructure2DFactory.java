package au.edu.federation.caliko.demo2d;

/**
 * @author jsalvo
 */
public class CalikoDemoStructure2DFactory {
	
	public static CalikoDemoStructure2D makeDemoStructure2D(int demoNumber) throws Exception {
		CalikoDemoStructure2DEnum aDemo = findEnumForDemoNumber(demoNumber);
		if(aDemo !=null ) {
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
	
	private enum CalikoDemoStructure2DEnum {
		
		FIXEDBASE_ABSOLUTEBASEBONEJOINT_CONSTRAINTS(FixedBaseAbsoluteBaseBoneJointConstraints.class,1),
		FIXEDBASE_UNCONSTRAINEDBASEBONE_UNCONSTRAINEDBONES(FixedBaseUnconstrainedBaseBaseMultipleUnconstrainedBones.class, 2),
		FIXEDBASE_UNCONSTRAINEDBASEBONE_CONSTRAINEDBONES(FixedBaseUnconstrainedBaseBoneMultipleConstrainedBones.class, 3),
		MULTIPLECONNECTEDCHAINS_NOBASEBONE_CONSTRAINTS(MultipleConnectedChainsNoBaseBoneConstraints.class, 4),
		MULTIPLECONNECTEDCHAINS_LOCALRELATIVEBASEBONE_CONSTRAINTS(MultipleConnectedChainsLocalRelativeBaseBoneConstraints.class, 5),
		MULTIPLECONNECTEDCHAINS_LOCALABSOLUTEBASEBONE_CONSTRAINTS(MultipleConnectedChainsLocalAbsoluteBaseBoneConstraints.class, 6),
		VARYINGOFFSET_FIXEDCHAINS_WITHEMBEDDED_TARGETS(VaryingOffsetFixedChainsWithEmbeddedTargets.class, 7),
		MULTIPLENESTEDCHAINS_SEMIRANDOM(MultipleNestedChainsSemiRandom.class, 8);
		
		
		Class<? extends CalikoDemoStructure2D> clazz;
		int demoNumber;
		
		private <T extends CalikoDemoStructure2D> CalikoDemoStructure2DEnum(Class<T> clazz, int demoNumber) {
			this.clazz = clazz;
			this.demoNumber = demoNumber;
		}
		
		public Class<? extends CalikoDemoStructure2D> getClazz() {
			return this.clazz;
		}
		
		public int getDemoNumber() {
			return this.demoNumber;
		}
		
	}

}
