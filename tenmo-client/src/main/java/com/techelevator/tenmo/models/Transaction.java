package com.techelevator.tenmo.models;


public class Transaction {
	
	private long id;
	private String senderName;
	private String receiverName;
	private long senderAccountId;
	private long recieverAccountId;
//	private TransactionType type;
//	private TransactionStatus transferStatus;
	private int typeId;
	private int statusId;
	private double transferAmount;
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public double getTransferAmount() {
		return transferAmount;
	}
	public void setTransferAmount(double transferAmount) {
		this.transferAmount = transferAmount;
	}
//	public TransactionType getType() {
//		return type;
//	}
//	public void setType(TransactionType type) {
//		this.type = type;
//	}
//	public TransactionStatus getTransferStatus() {
//		return transferStatus;
//	}
//	public void setTransferStatus(TransactionStatus transferStatus) {
//		this.transferStatus = transferStatus;
//	}
	public long getSenderAccountId() {
		return senderAccountId;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public int getStatusId() {
		return statusId;
	}
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
	public void setSenderAccountId(long senderAccountId) {
		this.senderAccountId = senderAccountId;
	}
	public long getRecieverAccountId() {
		return recieverAccountId;
	}
	public void setRecieverAccountId(long recieverAccountId) {
		this.recieverAccountId = recieverAccountId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((receiverName == null) ? 0 : receiverName.hashCode());
		result = prime * result + (int) (recieverAccountId ^ (recieverAccountId >>> 32));
		result = prime * result + (int) (senderAccountId ^ (senderAccountId >>> 32));
		result = prime * result + ((senderName == null) ? 0 : senderName.hashCode());
		result = prime * result + statusId;
		long temp;
		temp = Double.doubleToLongBits(transferAmount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + typeId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (id != other.id)
			return false;
		if (receiverName == null) {
			if (other.receiverName != null)
				return false;
		} else if (!receiverName.equals(other.receiverName))
			return false;
		if (recieverAccountId != other.recieverAccountId)
			return false;
		if (senderAccountId != other.senderAccountId)
			return false;
		if (senderName == null) {
			if (other.senderName != null)
				return false;
		} else if (!senderName.equals(other.senderName))
			return false;
		if (statusId != other.statusId)
			return false;
		if (Double.doubleToLongBits(transferAmount) != Double.doubleToLongBits(other.transferAmount))
			return false;
		if (typeId != other.typeId)
			return false;
		return true;
	}
	
	

}
