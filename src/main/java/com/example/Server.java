package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {

    public static HashMap<String, Integer> disponibilita = new HashMap<>();
    public static ArrayList<String> username = new ArrayList<>();
    private Socket clientSocket;

    public Server(Socket s) {
        this.clientSocket = s;
    }

    public static void AspettaClient() {
        try {
            ServerSocket ss = new ServerSocket(3000);

            while (true) {
                try {
                    Socket client = ss.accept();
                    Server gestoreClient = new Server(client);
                    Thread t = new Thread(gestoreClient);
                    t.start();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void scrivimsg(Socket s, String msg) {
        try {
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(msg);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void stampaBigliettiRimasti(Socket s) {
        String msg = "AVAIL ";
        for (String biglietti : disponibilita.keySet()) {
            msg += " " + biglietti + ":" + disponibilita.get(biglietti);
        }
        scrivimsg(s, msg);
    }

    public static void leggiClient(Socket s) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = in.readLine();
            if (line == null) return;
            System.out.println(line);

            if (!username.contains(line)) {
                ArrayList<String> tmp = new ArrayList<>();
                username.add(line);
                scrivimsg(s, "cosa vuoi fare?");

                while (true) {
                    line = in.readLine();
                    if (line == null) break;
                    System.out.println(line);

                    if (line.equals("quit")) {
                        s.close();
                        break;
                    } else if (line.equals("N")) {
                        stampaBigliettiRimasti(s);
                    } else if (line.equals("BUY")) {
                        line = in.readLine();
                        if (line != null) {
                            tmp.clear();
                            for (String parola : line.split(" ")) {
                                tmp.add(parola);
                            }

                            if (tmp.size() >= 2) {
                                String tipologia = tmp.get(0);
                                int quantita = Integer.parseInt(tmp.get(1));

                                 if (disponibilita.containsKey(tipologia) && quantita <= disponibilita.get(tipologia)) {
                                  
                                        int quantitaRimasta = disponibilita.get(tipologia) - quantita;
                                        disponibilita.put(tipologia, quantitaRimasta);
                                       scrivimsg(s, "OK");
                                      
                                    
                                }else{
                                
                                scrivimsg(s,"KO");
                            }
                        }
                        }
                    } else {
                        scrivimsg(s, "Comando non riconosciuto. Usa N, BUY o quit.");
                    }
                }
            } else {
                scrivimsg(s, "username Già esistente");
                s.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        System.out.println("server avviato per socket " + clientSocket.getInetAddress());
        leggiClient(clientSocket); 
    }

    public static void main(String[] args) {
        System.out.println("Server pronto sulla porta 3000...");
        disponibilita.put("Gold", 10);
        disponibilita.put("Pit", 30);
        disponibilita.put("Parterre", 60);
        AspettaClient();
    }
}
