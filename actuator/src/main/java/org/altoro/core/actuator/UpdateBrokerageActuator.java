package org.altoro.core.actuator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.altoro.common.utils.DecodeUtil;
import org.altoro.core.capsule.AccountCapsule;
import org.altoro.core.capsule.TransactionResultCapsule;
import org.altoro.core.capsule.WitnessCapsule;
import org.altoro.core.exception.ContractExeException;
import org.altoro.core.exception.ContractValidateException;
import org.altoro.core.store.AccountStore;
import org.altoro.core.store.DelegationStore;
import org.altoro.core.store.DynamicPropertiesStore;
import org.altoro.core.store.WitnessStore;
import org.altoro.protos.Protocol.Transaction.Contract.ContractType;
import org.altoro.protos.Protocol.Transaction.Result.code;
import org.altoro.protos.contract.StorageContract.UpdateBrokerageContract;
import org.spongycastle.util.encoders.Hex;

import java.util.Objects;


@Slf4j(topic = "actuator")
public class UpdateBrokerageActuator extends AbstractActuator {

  public UpdateBrokerageActuator() {
    super(ContractType.UpdateBrokerageContract, UpdateBrokerageContract.class);
  }

  @Override
  public boolean execute(Object result) throws ContractExeException {
    TransactionResultCapsule ret = (TransactionResultCapsule) result;
    if (Objects.isNull(ret)) {
      throw new RuntimeException(ActuatorConstant.TX_RESULT_NULL);
    }

    final UpdateBrokerageContract updateBrokerageContract;
    final long fee = calcFee();

    DelegationStore delegationStore = chainBaseManager.getDelegationStore();

    try {
      updateBrokerageContract = any.unpack(UpdateBrokerageContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }

    byte[] ownerAddress = updateBrokerageContract.getOwnerAddress().toByteArray();
    int brokerage = updateBrokerageContract.getBrokerage();

    delegationStore.setBrokerage(ownerAddress, brokerage);
    ret.setStatus(fee, code.SUCESS);

    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (this.any == null) {
      throw new ContractValidateException(ActuatorConstant.CONTRACT_NOT_EXIST);
    }
    if (chainBaseManager == null) {
      throw new ContractValidateException(ActuatorConstant.STORE_NOT_EXIST);
    }
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    WitnessStore witnessStore = chainBaseManager.getWitnessStore();
    if (!dynamicStore.allowChangeDelegation()) {
      throw new ContractValidateException(
              "contract type error, unexpected type [UpdateBrokerageContract]");
    }

    if (!this.any.is(UpdateBrokerageContract.class)) {
      throw new ContractValidateException(
              "contract type error, expected type [UpdateBrokerageContract], real type[" + any
                      .getClass() + "]");
    }
    final UpdateBrokerageContract updateBrokerageContract;
    try {
      updateBrokerageContract = any.unpack(UpdateBrokerageContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    byte[] ownerAddress = updateBrokerageContract.getOwnerAddress().toByteArray();
    int brokerage = updateBrokerageContract.getBrokerage();

    if (!DecodeUtil.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid ownerAddress");
    }

    if (brokerage < 0 || brokerage > ActuatorConstant.ONE_HUNDRED) {
      throw new ContractValidateException("Invalid brokerage");
    }

    WitnessCapsule witnessCapsule = witnessStore.get(ownerAddress);
    if (witnessCapsule == null) {
      throw new ContractValidateException("Not existed witness:" + Hex.toHexString(ownerAddress));
    }

    AccountCapsule account = accountStore.get(ownerAddress);
    if (account == null) {
      throw new ContractValidateException("Account does not exist");
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return any.unpack(UpdateBrokerageContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }
}