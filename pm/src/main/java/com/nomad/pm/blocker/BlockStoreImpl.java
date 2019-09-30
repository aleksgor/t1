package com.nomad.pm.blocker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.BlockInvoker;
import com.nomad.model.Identifier;


public class BlockStoreImpl  implements BlockInvoker{

//	private long timeout=50; // in ms
	private static final String NO_SESSION="!!NOSES!";
	protected static Logger LOGGER = LoggerFactory.getLogger(BlockInvoker.class);

  private final Map<Identifier, Map<String, Block>> blocks = new HashMap<>();

	@Override
	public boolean softBlock(String sessionId, final Identifier id) {
		if(sessionId==null){
			sessionId=NO_SESSION;
		}
		synchronized (blocks) {
			 Map<String, Block> objectBlocks=blocks.get(id);
			 if(objectBlocks==null){
        objectBlocks = new HashMap<>();
				 objectBlocks.put(sessionId, new Block(false,sessionId));
				 blocks.put(id, objectBlocks);
				 return true;
			 }else{
				 if(objectBlocks.size()==0){
					 objectBlocks.put(sessionId, new Block(false,sessionId));
					 return true;
				 }
				 final Block blockCounter = objectBlocks.get(sessionId);
				 if(blockCounter != null){ // blocked same session
					 blockCounter.addSoftBlock();
					 objectBlocks.put(sessionId, blockCounter);
					 return true;
				 }else{
					 if(objectBlocks.size()==1){
						 final Block aloneBlock=objectBlocks.values().iterator().next();
						 if(aloneBlock.isHasHardBlock()){
							 return false;
						 }
					 }
					 objectBlocks.put(sessionId, blockCounter);
					 return true;

				 }
			 }
		}

	}


	@Override
	public  boolean hardBlock(String sessionId,final Identifier id) {
		if(sessionId==null){
			sessionId=NO_SESSION;
		}
		synchronized (blocks) {
			 Map<String, Block> objectBlocks=blocks.get(id);
			 if(objectBlocks==null){
        objectBlocks = new HashMap<>();
				 objectBlocks.put(sessionId, new Block(true,sessionId));
				 blocks.put(id, objectBlocks);
				 return true;
			 }else{
				 if(objectBlocks.size()==0){
					 objectBlocks.put(sessionId, new Block(true,sessionId));
					 return true;
				 }
				 final Block blocker = objectBlocks.get(sessionId);
				 if(blocker != null){ // blocked same session
					 blocker.addHardBlock();
					 objectBlocks.put(sessionId, blocker);
					 return true;
				 }else{
					 return false;

				 }
			 }
		}

	}

	@Override
	public void softUnlock(String sessionId,final Identifier id){
		if(sessionId==null){
			sessionId=NO_SESSION;
		}
		synchronized (blocks) {
			 final Map<String, Block> objectBlocks=blocks.get(id);
			 if(objectBlocks==null){
				 LOGGER.warn("Object not locked!{}",id);
				 return ;
			 }else{
				 if(objectBlocks.size()==0){
					 LOGGER.warn("Object not locked!{}",id);
					 return ;
				 }
				 final Block blocker = objectBlocks.get(sessionId);
				 if(blocker != null){ // blocked same session
					 if(blocker.isHasHardBlock() ){
						 return ;
					 }
					 if(blocker.softUnlock()){
						 objectBlocks.remove(id);
						 return;
					 }
					 objectBlocks.put(sessionId, blocker);
				 }else{
					 LOGGER.warn("Object not locked! {} in session {}",id,sessionId);
					 return ;
				 }
			 }
		}
	}

	@Override
	public void hardUnlock(String sessionId,final Identifier id){
		if(sessionId==null){
			sessionId=NO_SESSION;
		}
		synchronized (blocks) {
			 final Map<String, Block> objectBlocks=blocks.get(id);
			 if(objectBlocks==null){
				 LOGGER.warn("Object not locked!{}",id);
				 return ;
			 }else{
				 if(objectBlocks.size()==0){
					 LOGGER.warn("Object not locked!{}",id);
					 return ;
				 }
				 final Block blockCounter = objectBlocks.get(sessionId);
				 if(blockCounter != null){ // blocked same session
					 if(blockCounter.hardUnlock()){
						 blocks.remove(id);
						 return;
					 }
					 objectBlocks.put(sessionId, blockCounter);
				 }else{
					 LOGGER.warn("Object not locked! {} in session {}",id,sessionId);
					 return ;
				 }
			 }
		}

	}


	private class Block{

		private int counter;
    public Block(final boolean hardBlock, final String sessionId){
			if(hardBlock){
				counter=-1;
			}else{
				counter=1;
			}
		}
		public boolean addSoftBlock(){
			if(counter>=0){
				counter++;
				return true;
			}
			return false;
		}
		public void addHardBlock(){
			if(counter>0){
				counter=0;
			}
			counter--;
		}

/*		public boolean isHasSoftBlock(){
			return (counter>0);
		}
*/
		public boolean isHasHardBlock(){
			return (counter<0);
		}

		public boolean hardUnlock(){
			if(counter<0){
				counter++;
			}
			return counter==0;
		}
		public boolean softUnlock(){
			if(counter>0){
				counter--;
			}
			return counter==0;
		}

	}
	@Override
  public void cleanSession(final String sessionId){
		blocks.remove(sessionId);
	}

}
