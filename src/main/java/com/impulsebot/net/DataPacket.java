package com.impulsebot.net;

import java.io.Serializable;

import com.impulsebot.net.client.ClientCustomPacket;
import com.impulsebot.net.server.ServerCustomPacket;
import com.impulsebot.utils.Main;

public class DataPacket<T> implements Serializable {

	private static final long	serialVersionUID	= -6909492759537219041L;
	private final Object		serializedData;
	private final Class			clazz;
	private final boolean		useXML;

	public DataPacket(T o) {
		this(o, o instanceof ClientCustomPacket || o instanceof ServerCustomPacket);
	}

	/**
	 * When using XML, serialize using XStream instead of Kryo, the size of the
	 * packet can be significantly larger than when using byte arrays, serialize
	 * using Kryo instead of XStream.
	 *
	 * @param o
	 *            The object to be sent.
	 * @param useXML
	 *            Whether to serialize using XStream which outputs XML or serialize
	 *            with Kryo which outputs byte arrays.
	 */
	public DataPacket(T o, boolean useXML) {
		serializedData = useXML ? Main.getXStream().toXML(o) : Main.serializeObjectWithKryo(o);
		clazz = o.getClass();
		this.useXML = useXML;
	}

	public Object getSerializedData() {
		return serializedData;
	}

	public Class getClazz() {
		return clazz;
	}

	public boolean useXML() {
		return useXML;
	}

	public T toObject() {
		return useXML ? (T) Main.getXStream().fromXML((String) serializedData) : Main.deserializeByteArrayWithKryo((byte[]) serializedData);
	}

}
