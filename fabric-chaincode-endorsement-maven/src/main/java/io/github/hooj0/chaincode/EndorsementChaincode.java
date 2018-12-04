package io.github.hooj0.chaincode;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement.RoleType;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import com.google.gson.JsonArray;

/**
 * endorsement chaincode examples
 * @author hoojo
 * @createDate 2018年12月4日 上午11:14:32
 * @file EndorsementChaincode.java
 * @package io.github.hooj0.chaincode
 * @project fabric-chaincode-endorsement-maven
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class EndorsementChaincode extends ChaincodeBase {

	private static final Log log = LogFactory.getLog(EndorsementChaincode.class);
	
	private static final String STATE_NAME = "endorsed_state";
	
	@Override
	public Response init(ChaincodeStub stub) {
		log.info(">>> init endorsement chaincode");
		
		try {
			stub.putStringState(STATE_NAME, "foo");
			
			return newSuccessResponse();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}

	@Override
	public Response invoke(ChaincodeStub stub) {
		log.info(">>> invoke endorsement chaincode");
		
		try {
			
			String func = stub.getFunction();
			
			Method method = EndorsementChaincode.class.getMethod(func, ChaincodeStub.class);
			Response response = (Response) method.invoke(this, stub);
			
			return response;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	public Response addOrg(ChaincodeStub stub) {
		log.info(">>> endorsement chaincode invoking addOrg method.");
		
		try {
			List<String> params = stub.getParameters();
			if (params.isEmpty()) {
				return newErrorResponse("add orgs params is empty!");
			}
			
			byte[] bytes = this.getEndorsement(stub);
			
			StateBasedEndorsement endorsement = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(bytes);
			endorsement.addOrgs(RoleType.RoleTypePeer, params.toArray(new String[] {}));
			
			this.setEndorsement(stub, endorsement.policy());
			
			return newSuccessResponse();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	public Response removeOrg(ChaincodeStub stub) {
		log.info(">>> endorsement chaincode invoking remove Org method.");
		
		try {
			List<String> params = stub.getParameters();
			if (params.isEmpty()) {
				return newErrorResponse("remove orgs params is empty!");
			}
			
			byte[] bytes = this.getEndorsement(stub);
			
			StateBasedEndorsement endorsement = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(bytes);
			endorsement.delOrgs(params.toArray(new String[] {}));
			
			this.setEndorsement(stub, endorsement.policy());
			
			return newSuccessResponse();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	public Response listOrg(ChaincodeStub stub) {
		log.info(">>> endorsement chaincode invoking listOrg method.");
		
		try {
			byte[] bytes = this.getEndorsement(stub);
			
			StateBasedEndorsement endorsement = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(bytes);
			List<String> orgs = endorsement.listOrgs();
			
			JsonArray array = new JsonArray();
			orgs.forEach(e -> orgs.add(e));
			
			return newSuccessResponse(array.toString().getBytes());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	public Response clean(ChaincodeStub stub) {
		log.info(">>> endorsement chaincode invoking clean method.");
		
		try {
			this.setEndorsement(stub, null);
			
			return newSuccessResponse();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	public Response get(ChaincodeStub stub) {
		log.info(">>> endorsement chaincode invoking get method.");
		
		try {
			return newSuccessResponse(stub.getState(STATE_NAME));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	public Response set(ChaincodeStub stub) {
		log.info(">>> endorsement chaincode invoking set method.");
		
		try {
			List<String> params = stub.getParameters();
			if (params.size() != 1) {
				return newErrorResponse("set params size Extra long!");
			}
			
			stub.putStringState(STATE_NAME, params.get(0));
			
			return newSuccessResponse();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return newErrorResponse(e);
		}
	}
	
	private byte[] getEndorsement(ChaincodeStub stub) {
		//return stub.getStateValidationParameter(STATE_NAME);
		return new byte[0];
	}
	
	private void setEndorsement(ChaincodeStub stub, byte[] bytes) {
		//stub.setStateValidationParameter(STATE_NAME, bytes);
	}
}
