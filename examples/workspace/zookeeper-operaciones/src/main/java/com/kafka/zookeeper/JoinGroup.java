package com.kafka.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;

public	class	JoinGroup	extends	ConnectionWatcher	{
		
		public void	join(String	groupName, String memberName) throws KeeperException,
						InterruptedException	{
				String	path = "/" + groupName + "/" + memberName;
				String	createdPath	= zk.create(path,	null/*data*/, Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL);
				System.out.println("Created	" +	createdPath);
		}
		
		public static void main(String[] args) throws Exception	{
				JoinGroup joinGroup	= new JoinGroup();
				joinGroup.connect(args[0]);
				joinGroup.join(args[1],	args[2]);
				
				//	Se mantiene durmiendo hasta que el proceso sea matado o interrumpido el hilo de ejecuciión
				Thread.sleep(Long.MAX_VALUE);
		}
}