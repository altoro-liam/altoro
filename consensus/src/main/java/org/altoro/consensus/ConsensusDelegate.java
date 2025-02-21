package org.altoro.consensus;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.altoro.core.capsule.AccountCapsule;
import org.altoro.core.capsule.WitnessCapsule;
import org.altoro.core.store.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j(topic = "consensus")
@Component
public class ConsensusDelegate {

  @Autowired
  private DynamicPropertiesStore dynamicPropertiesStore;

  @Autowired
  private DelegationStore delegationStore;

  @Autowired
  private AccountStore accountStore;

  @Autowired
  private WitnessStore witnessStore;

  @Autowired
  private WitnessScheduleStore witnessScheduleStore;

  @Autowired
  private VotesStore votesStore;


  public DynamicPropertiesStore getDynamicPropertiesStore() {
    return dynamicPropertiesStore;
  }

  public DelegationStore getDelegationStore() {
    return delegationStore;
  }

  public VotesStore getVotesStore() {
    return votesStore;
  }

  public int calculateFilledSlotsCount() {
    return dynamicPropertiesStore.calculateFilledSlotsCount();
  }

  public void saveRemoveThePowerOfTheGr(long rate) {
    dynamicPropertiesStore.saveRemoveThePowerOfTheGr(rate);
  }

  public long getRemoveThePowerOfTheGr() {
    return dynamicPropertiesStore.getRemoveThePowerOfTheGr();
  }

  public long getWitnessStandbyAllowance() {
    return dynamicPropertiesStore.getWitnessStandbyAllowance();
  }

  public long getLatestBlockHeaderTimestamp() {
    return dynamicPropertiesStore.getLatestBlockHeaderTimestamp();
  }

  public long getLatestBlockHeaderNumber() {
    return dynamicPropertiesStore.getLatestBlockHeaderNumber();
  }

  public boolean lastHeadBlockIsMaintenance() {
    return dynamicPropertiesStore.getStateFlag() == 1;
  }

  public long getMaintenanceSkipSlots() {
    return dynamicPropertiesStore.getMaintenanceSkipSlots();
  }

  public void saveActiveWitnesses(List<ByteString> addresses) {
    witnessScheduleStore.saveActiveWitnesses(addresses);
  }

  public List<ByteString> getActiveWitnesses() {
    return witnessScheduleStore.getActiveWitnesses();
  }

  public AccountCapsule getAccount(byte[] address) {
    return accountStore.get(address);
  }

  public void saveAccount(AccountCapsule accountCapsule) {
    accountStore.put(accountCapsule.createDbKey(), accountCapsule);
  }

  public WitnessCapsule getWitness(byte[] address) {
    return witnessStore.get(address);
  }

  public void saveWitness(WitnessCapsule witnessCapsule) {
    witnessStore.put(witnessCapsule.createDbKey(), witnessCapsule);
  }

  public List<WitnessCapsule> getAllWitnesses() {
    return witnessStore.getAllWitnesses();
  }

  public void saveStateFlag(int flag) {
    dynamicPropertiesStore.saveStateFlag(flag);
  }

  public void updateNextMaintenanceTime(long time) {
    dynamicPropertiesStore.updateNextMaintenanceTime(time);
  }

  public long getNextMaintenanceTime() {
    return dynamicPropertiesStore.getNextMaintenanceTime();
  }

  public long getLatestSolidifiedBlockNum() {
    return dynamicPropertiesStore.getLatestSolidifiedBlockNum();
  }

  public void saveLatestSolidifiedBlockNum(long num) {
    dynamicPropertiesStore.saveLatestSolidifiedBlockNum(num);
  }

  public void applyBlock(boolean flag) {
    dynamicPropertiesStore.applyBlock(flag);
  }

  public boolean allowChangeDelegation() {
    return dynamicPropertiesStore.allowChangeDelegation();
  }
}