package serverProposta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class DiscoveryServer {

		public static void main(String[] args) {

			System.out.println("DiscoveryServer: avviato");

			DatagramSocket socket = null;
			DatagramPacket packet = null;
			byte[] buf = new byte[256];
			int port = -1;
			Thread[] rowSwapServerArr = new Thread[(args.length-2) / 2];

			if (args.length % 2 == 1) throw new IllegalArgumentException("Numero di argomenti dispari");
			try {
				port = Integer.parseInt(args[1]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java DiscoveryServer [serverPort>1024]");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.out.println("Usage: java DiscoveryServer [serverPort>1024]");
				System.exit(1);
			}

			// popolo la mappa con i file e le porte
			HashMap<String, Integer> hashmap = new HashMap<>();
			for (int i = 2; i < args.length; i += 2) 
				if (hashmap.putIfAbsent(args[i], Integer.parseInt(args[i+1])) != null)
					throw new IllegalArgumentException("Porta duplicata");
			
			try {
				socket = new DatagramSocket(port);
				packet = new DatagramPacket(buf, buf.length);
				System.out.println("Creata la socket: " + socket);
			}
			catch (SocketException e) {
				System.out.println("Problemi nella creazione della socket: ");
				e.printStackTrace();
				System.exit(1);
			}
			
			// lancio dei SR
			for (int i = 2; i < args.length; i += 2) {
				rowSwapServerArr[i] = new RowSwapServer(Integer.parseInt(args[i+1]), args[i]);
				rowSwapServerArr[i].start();
			}
			
			try {
				String richiesta = null;
				ByteArrayInputStream biStream = null;
				DataInputStream diStream = null;
				ByteArrayOutputStream boStream = null;
				DataOutputStream doStream = null;
				byte[] data = null;

				while (true) {
					System.out.println("\nIn attesa di richieste...");
					
					// ricezione del datagramma
					try {
						packet.setData(buf);
						socket.receive(packet);
					}
					catch (IOException e) {
						System.err.println("Problemi nella ricezione del datagramma: "
								+ e.getMessage());
						e.printStackTrace();
						continue;
						// il server continua a fornire il servizio ricominciando dall'inizio
						// del ciclo
					}

					try {
						biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
						diStream = new DataInputStream(biStream);
						richiesta = diStream.readUTF();
						System.out.println("Richiesta: file" + richiesta + " presente alla porta " + hashmap.get(richiesta));
					}
					catch (Exception e) {
						System.err.println("Problemi nella lettura della richiesta: file" + richiesta + " presente alla porta " + hashmap.get(richiesta));
						e.printStackTrace();
						continue;
						// il server continua a fornire il servizio ricominciando dall'inizio
						// del ciclo
					}

					// invio della risposta
					try {
						boStream = new ByteArrayOutputStream();
						doStream = new DataOutputStream(boStream);
						doStream.writeUTF(String.valueOf(hashmap.get(richiesta)));
						data = boStream.toByteArray();
						packet.setData(data, 0, data.length);
						socket.send(packet);
					}
					catch (IOException e) {
						System.err.println("Problemi nell'invio della risposta: "
					      + e.getMessage());
						e.printStackTrace();
						continue;
						// il server continua a fornire il servizio ricominciando dall'inizio
						// del ciclo
					}

				}

			}
			// qui catturo le eccezioni non catturate all'interno del while
			// in seguito alle quali il server termina l'esecuzione
			catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("DiscoveryServer: termino...");
			socket.close();
		}
	
}
