package org.altoro.core.actuator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.altoro.common.utils.DecodeUtil;
import org.altoro.common.utils.StringUtil;
import org.altoro.core.capsule.AccountCapsule;
import org.altoro.core.capsule.ContractCapsule;
import org.altoro.core.capsule.TransactionResultCapsule;
import org.altoro.core.exception.ContractExeException;
import org.altoro.core.exception.ContractValidateException;
import org.altoro.core.store.AccountStore;
import org.altoro.core.store.ContractStore;
import org.altoro.core.vm.config.VMConfig;
import org.altoro.protos.Protocol.Transaction.Contract.ContractType;
import org.altoro.protos.Protocol.Transaction.Result.code;
import org.altoro.protos.contract.SmartContractOuterClass.ClearABIContract;

import java.util.Arrays;
import java.util.Objects;

@Slf4j(topic = "actuator")
public class ClearABIContractActuator extends AbstractActuator {

  public ClearABIContractActuator() {
    super(ContractType.ClearABIContract, ClearABIContract.class);
  }

  @Override
  public boolean execute(Object result) throws ContractExeException {
    TransactionResultCapsule ret = (TransactionResultCapsule) result;
    if (Objects.isNull(ret)) {
      throw new RuntimeException(ActuatorConstant.TX_RESULT_NULL);
    }

    long fee = calcFee();
    ContractStore contractStore = chainBaseManager.getContractStore();
    try {
      ClearABIContract usContract = any.unpack(ClearABIContract.class);

      byte[] contractAddress = usContract.getContractAddress().toByteArray();
      ContractCapsule deployedContract = contractStore.get(contractAddress);

      deployedContract.clearABI();
      contractStore.put(contractAddress, deployedContract);

      ret.setStatus(fee, code.SUCESS);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }
    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (!VMConfig.allowTvmConstantinople()) {
      throw new ContractValidateException(
              "contract type error,unexpected type [ClearABIContract]");
    }

    if (this.any == null) {
      throw new ContractValidateException(ActuatorConstant.CONTRACT_NOT_EXIST);
    }
    if (chainBaseManager == null) {
      throw new ContractValidateException("No account store or contract store!");
    }
    AccountStore accountStore = chainBaseManager.getAccountStore();
    ContractStore contractStore = chainBaseManager.getContractStore();
    if (!this.any.is(ClearABIContract.class)) {
      throw new ContractValidateException(
              "contract type error,expected type [ClearABIContract],real type["
                      + any.getClass() + "]");
    }
    final ClearABIContract contract;
    try {
      contract = this.any.unpack(ClearABIContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    if (!DecodeUtil.addressValid(contract.getOwnerAddress().toByteArray())) {
      throw new ContractValidateException("Invalid address");
    }
    byte[] ownerAddress = contract.getOwnerAddress().toByteArray();
    String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);

    AccountCapsule accountCapsule = accountStore.get(ownerAddress);
    if (accountCapsule == null) {
      throw new ContractValidateException(
              ActuatorConstant.ACCOUNT_EXCEPTION_STR
                      + readableOwnerAddress + "] not exists");
    }

    byte[] contractAddress = contract.getContractAddress().toByteArray();
    ContractCapsule deployedContract = contractStore.get(contractAddress);

    if (deployedContract == null) {
      throw new ContractValidateException(
              "Contract not exists");
    }

    byte[] deployedContractOwnerAddress = deployedContract.getInstance().getOriginAddress()
            .toByteArray();

    if (!Arrays.equals(ownerAddress, deployedContractOwnerAddress)) {
      throw new ContractValidateException(
              ActuatorConstant.ACCOUNT_EXCEPTION_STR
                      + readableOwnerAddress + "] is not the owner of the contract");
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return any.unpack(ClearABIContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }

}

