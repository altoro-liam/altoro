package org.altoro.core.actuator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.altoro.common.utils.DecodeUtil;
import org.altoro.core.capsule.AccountCapsule;
import org.altoro.core.capsule.TransactionResultCapsule;
import org.altoro.core.exception.ContractExeException;
import org.altoro.core.exception.ContractValidateException;
import org.altoro.core.store.AccountIdIndexStore;
import org.altoro.core.store.AccountStore;
import org.altoro.core.utils.TransactionUtil;
import org.altoro.protos.Protocol.Transaction.Contract.ContractType;
import org.altoro.protos.Protocol.Transaction.Result.code;
import org.altoro.protos.contract.AccountContract.SetAccountIdContract;

import java.util.Objects;

@Slf4j(topic = "actuator")
public class SetAccountIdActuator extends AbstractActuator {

  public SetAccountIdActuator() {
    super(ContractType.SetAccountIdContract, SetAccountIdContract.class);
  }

  @Override
  public boolean execute(Object result) throws ContractExeException {
    TransactionResultCapsule ret = (TransactionResultCapsule) result;
    if (Objects.isNull(ret)) {
      throw new RuntimeException(ActuatorConstant.TX_RESULT_NULL);
    }

    final SetAccountIdContract setAccountIdContract;
    final long fee = calcFee();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    AccountIdIndexStore accountIdIndexStore = chainBaseManager.getAccountIdIndexStore();
    try {
      setAccountIdContract = any.unpack(SetAccountIdContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }

    byte[] ownerAddress = setAccountIdContract.getOwnerAddress().toByteArray();
    AccountCapsule account = accountStore.get(ownerAddress);

    account.setAccountId(setAccountIdContract.getAccountId().toByteArray());
    accountStore.put(ownerAddress, account);
    accountIdIndexStore.put(account);
    ret.setStatus(fee, code.SUCESS);

    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (this.any == null) {
      throw new ContractValidateException(ActuatorConstant.CONTRACT_NOT_EXIST);
    }
    if (chainBaseManager == null) {
      throw new ContractValidateException("No account store or account id index store!");
    }
    AccountStore accountStore = chainBaseManager.getAccountStore();
    AccountIdIndexStore accountIdIndexStore = chainBaseManager.getAccountIdIndexStore();
    if (!this.any.is(SetAccountIdContract.class)) {
      throw new ContractValidateException(
              "contract type error,expected type [SetAccountIdContract],real type[" + any
                      .getClass() + "]");
    }
    final SetAccountIdContract setAccountIdContract;
    try {
      setAccountIdContract = any.unpack(SetAccountIdContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    byte[] ownerAddress = setAccountIdContract.getOwnerAddress().toByteArray();
    byte[] accountId = setAccountIdContract.getAccountId().toByteArray();
    if (!TransactionUtil.validAccountId(accountId)) {
      throw new ContractValidateException("Invalid accountId");
    }
    if (!DecodeUtil.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid ownerAddress");
    }

    AccountCapsule account = accountStore.get(ownerAddress);
    if (account == null) {
      throw new ContractValidateException("Account has not existed");
    }
    if (account.getAccountId() != null && !account.getAccountId().isEmpty()) {
      throw new ContractValidateException("This account id already set");
    }
    if (accountIdIndexStore.has(accountId)) {
      throw new ContractValidateException("This id has existed");
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return any.unpack(SetAccountIdContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }
}
