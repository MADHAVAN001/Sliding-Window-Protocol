/* Project done by
Name: Seshadri Madhavan
Matric. No: U1322790J
Date: 11/9/2015
*/


/*===============================================================*
 *  File: SWP.java                                               *
 *                                                               *
 *  This class implements the sliding window protocol            *
 *  Used by VMach class					         *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,     *
 *                                                               *
 *  Author: Professor SUN Chengzheng                             *
 *          School of Computer Engineering                       *
 *          Nanyang Technological University                     *
 *          Singapore 639798                                     *
 *===============================================================*/

import java.util.Timer;
import java.util.TimerTask;

public class SWP {

/*========================================================================*
 the following are provided, do not change them!!
 *========================================================================*/
   //the following are protocol constants.
   public static final int MAX_SEQ = 7; 
   public static final int NR_BUFS = (MAX_SEQ + 1)/2;

   // the following are protocol variables
   private int oldest_frame = 0;
   private PEvent event = new PEvent();  
   private Packet out_buf[] = new Packet[NR_BUFS];
   
   //declare in_buffer which is a packet data type
   private Packet in_buffer[] = new Packet[NR_BUFS];

   //the following are used for simulation purpose only
   private SWE swe = null;
   private String sid = null;  

   //Constructor
   public SWP(SWE sw, String s){
      swe = sw;
      sid = s;
   }

   //the following methods are all protocol related
   private void init(){
      for (int i = 0; i < NR_BUFS; i++){
	   out_buf[i] = new Packet();
	//initialize in buffer in arrays
	in_buffer[i] = new Packet();
      }
   }

   private void wait_for_event(PEvent e){
      swe.wait_for_event(e); //may be blocked
      oldest_frame = e.seq;  //set timeout frame seq
   }

   private void enable_network_layer(int nr_of_bufs) {
   //network layer is permitted to send if credit is available
	swe.grant_credit(nr_of_bufs);
   }

   private void from_network_layer(Packet p) {
      swe.from_network_layer(p);
   }

   private void to_network_layer(Packet packet) {
	swe.to_network_layer(packet);
   }

   private void to_physical_layer(PFrame fm)  {
      System.out.println("SWP: Sending frame: seq = " + fm.seq + 
			    " ack = " + fm.ack + " kind = " + 
			    PFrame.KIND[fm.kind] + " info = " + fm.info.data );
      System.out.flush();
      swe.to_physical_layer(fm);
   }

   private void from_physical_layer(PFrame fm) {
      PFrame fm1 = swe.from_physical_layer(); 
	fm.kind = fm1.kind;
	fm.seq = fm1.seq; 
	fm.ack = fm1.ack;
	fm.info = fm1.info;
   }


/*===========================================================================*
 	implement your Protocol Variables and Methods below: 
 *==========================================================================*/

	//Here the between method is defined for protocol 6. 		This implementation is the same for protocol 5. It checks 		the circular condition of the frame numbers.	
	
	public static boolean between(int x, int y, int z) {
        //queue to check circular condition
        return ((x <= y) && (y < z)) || ((z < x) && (x <= y))
                || ((y < z) && (z < x));
	    }

	//A java method to send the frames from
	private void send_frame(int f, int next_frame, int 		frame_expected, Packet buffer[]) 
	{
	
        PFrame s = new PFrame(); //Decaring temporary variable for 						frame
        
	s.kind = f; //There are 3 kinds of frames, namely data, 			ack or nak
        if (f == PFrame.DATA) {
            s.info = buffer[next_frame % NR_BUFS];
        }
        s.seq = next_frame; //only meaningful for data frames
        s.ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);
        if (f == PFrame.NAK) {
            no_nak = false; //one nak per frame
        }
        to_physical_layer(s); //transmit frame
        if (f == PFrame.DATA) {
            start_timer(next_frame);
        }
        stop_ack_timer(); //no need for separate ack frame
    }

    boolean no_nak = true; //no nak has been sent yet


   public void protocol6() {
        init();
	int expected_ack;	//expected frame acknowledgement
	int next_frame_send;	//Frame number of the next frame 					to be sent
	int expected_frame;	//frame expected to be recieved by 					the reciever
	int index;		//index of the buffer
	int upper_limit;	//upper limit of the buffer
	
	//keeping track of frames arrived
	boolean recieved[] = new boolean[NR_BUFS];

	//declare the temporary frame
	PFrame temp_frame = new PFrame();

	//initialize network layer
        enable_network_layer(NR_BUFS);

	//initialize the counter variables
	expected_ack = 0;
	next_frame_send = 0;
	expected_frame = 0;
	upper_limit = NR_BUFS;
	index = 0;
	for(int i = 0;i<NR_BUFS;i++)
		recieved[i] = false;

	//Protocol rules for each of the parties involved
	while(true) {	
         wait_for_event(event);
	   switch(event.type) {
	      case (PEvent.NETWORK_LAYER_READY):
		//this happens in the sender's side
		//when the network layer is ready, fetch new 			packets from network layer and put the fetched 			packets in the out buffer of the sender
		from_network_layer(out_buf[next_frame_send % 			NR_BUFS]);
		//transmit the data fetched which is in the 			senders' buffer from the network layer over the 		network
                send_frame(PFrame.DATA, next_frame_send, 			expected_frame, out_buf);
		//the next frame to be sent from the buffer is 			incremented
		next_frame_send = inc(next_frame_send); 
                break; 
	      case (PEvent.FRAME_ARRIVAL):
		//this happens in the reciever's side
		from_physical_layer(temp_frame);

		//if the recieved frame is a data frame from the 			sender		
		if (temp_frame.kind == PFrame.DATA) 
		{
                    //A complete and undamaged frame is recieved
                    if ((temp_frame.seq != expected_frame) && 				no_nak)
		    {
                        send_frame(PFrame.NAK, 0, expected_frame, 				out_buf);
                    } 
		    else
		    //If the recieved frame is not a data frame 
		    {
                        start_ack_timer();
                    }

		    //Check if the frame recieved is between the 				expected frames of the sliding window and 				recieved frame has not been previosly 				recieved
		if (between(expected_frame, temp_frame.seq, 			upper_limit) && (recieved[temp_frame.seq % 			NR_BUFS] == false)) {
                        //This allows the frames to be accepted in 				any order of arrival in the reciever 
                        recieved[temp_frame.seq % NR_BUFS] = true; 
                        //If the data frame recieved is not 				damaged and falls between the expected 				frame and the limit of the sliding window 				buffer, then add the frame into the input 				buffer 
                        in_buffer[temp_frame.seq % NR_BUFS] = 							temp_frame.info; 
                        while (recieved[expected_frame % NR_BUFS]) 				{
                   	//Pass frames recieved from the physical 				layer by the sender to the network layer 
                            to_network_layer(in_buffer		[expected_frame % NR_BUFS]);
			    
                            no_nak = true;
			//mark the undamaged data frame recieved 				by the reciever as recieved
                            recieved[expected_frame % NR_BUFS] = 					false;
                        //Increment the lower expected in the 				sliding window when the complete expected 				frame is recieved by the reciever machine 
                            expected_frame = inc(expected_frame); 
                        //When a complete and undamaged frame is 				recieved, the upper edge of the sliding 			window is also incremented 
                            upper_limit = inc(upper_limit); 
                        //start the ack timer
                            start_ack_timer(); 
                        }
                    }
		}


                //If the frame in the reciever's side is NAK, then 			check that the frame is in between the current 			sliding window and resend the data of the frame 		for which the NAK has been recieved
                if ((temp_frame.kind == PFrame.NAK)
                   && between(expected_ack,
                   ((temp_frame.ack + 1) % (MAX_SEQ		+1)), next_frame_send)) 
		{
			//send the data of the frame for which NAK 				has been recieved by the sender of the data
                    send_frame(PFrame.DATA, ((temp_frame.ack + 1) 				% (MAX_SEQ + 1)),
                            expected_frame, out_buf);
                }
        	
		while (between(expected_ack, temp_frame.ack, 				next_frame_send)) {
                    //If a complete and undamaged frame is 				recieved 
                    stop_timer(expected_ack % NR_BUFS); 

                    //In the sender's sliding window increase the 				ecpected sliding window for the ack to be 				recieved for the sent data frames  
                    expected_ack = inc(expected_ack); 
                    
		    //always free 1 buffer slot if ack has been 			done 
                    enable_network_layer(1); 
                } 
		break;	   
            case (PEvent.CKSUM_ERR):
		if (no_nak) {
                    //damaged frame 
                    send_frame(PFrame.NAK, 0, expected_frame, 				out_buf); 
                }      	           
		break;  
            case (PEvent.TIMEOUT):
                //If the timer is expired for the oldest frame, 		then resend the data for the frame for which the 			timer has expired 
                send_frame(PFrame.DATA, oldest_frame, 			expected_frame, out_buf); 
                break;
            case (PEvent.ACK_TIMEOUT):
                //ack timer expired in the reciever's side for the 			frame that has been recieved, send the ACK again 			for the  
                send_frame(PFrame.ACK, 0, expected_frame, 			out_buf); 
                break; 
            default: 
		   System.out.println("SWP: undefined event type = "                                       + event.type); 
		   System.out.flush();
	   }
      }      
   }

 /* Note: when start_timer() and stop_timer() are called, 
    the "seq" parameter must be the sequence number, rather 
    than the index of the timer array, 
    of the frame associated with this timer, 
   */
    
    Timer frame_timer[] = new Timer[NR_BUFS];
    Timer ack_timer;
 
   public static int inc(int num) {
        num = ((num + 1) % (MAX_SEQ + 1));
        return num;
    }
   
private void start_timer(int seq) {
        stop_timer(seq);
        //create new timer
        frame_timer[seq % NR_BUFS] = new Timer();
        //schedule the task for execution after 200ms
        frame_timer[seq % NR_BUFS].schedule(new ReTask(swe, seq), 200);
    }

   private void stop_timer(int seq) {
	if (frame_timer[seq % NR_BUFS] != null) {
            frame_timer[seq % NR_BUFS].cancel();
         }
   }

   private void start_ack_timer( ) {
   	stop_ack_timer();
        //starts another timer for sending separate ack 
        ack_timer = new Timer();
        ack_timer.schedule(new AckTask(swe), 100);  
   }

   private void stop_ack_timer() {
     	if (ack_timer != null) {
            ack_timer.cancel();
        }
   }

//for retransmission timer
    class ReTask extends TimerTask {
        private SWE swe = null;
        public int seqnr;

        public ReTask(SWE sw, int seq) {
            swe = sw;
            seqnr = seq;
        }

        public void run() {
            //stops this timer and discard any 
            //scheduled tasks for the current seqnr
            stop_timer(seqnr);
            swe.generate_timeout_event(seqnr);
        }
    }

    //for ack timer 
    class AckTask extends TimerTask {

        private SWE swe = null;

        public AckTask(SWE sw) {
            swe = sw;
        }

        public void run() {
            // stop the timer 
            stop_ack_timer();
            swe.generate_acktimeout_event();
        }
    }
    


}//End of class

/* Note: In class SWE, the following two public methods are available:
   . generate_acktimeout_event() and
   . generate_timeout_event(seqnr).

   To call these two methods (for implementing timers),
   the "swe" object should be referred as follows:
     swe.generate_acktimeout_event(), or
     swe.generate_timeout_event(seqnr).
*/


