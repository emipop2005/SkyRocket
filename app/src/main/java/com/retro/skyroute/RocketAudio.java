package com.retro.skyroute;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.io.*;
import java.util.Random;

final class RocketAudio {
 private final SoundPool pool;
 private final int[] ids=new int[10];
 private final boolean[] ready=new boolean[10];
 private int engine=0, brake=0;
 private boolean active=true; private android.media.MediaPlayer music; private boolean musicWanted=false; private volatile boolean released=false;
 RocketAudio(Context ctx){
  pool=new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
  pool.setOnLoadCompleteListener((p,id,status)->{if(status==0)for(int i=0;i<ids.length;i++)if(ids[i]==id)ready[i]=true;});
  new Thread(()->{try{File m=new File(ctx.getCacheDir(),"sky_music_v1.wav");writeMusic(m);new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{if(released)return;try{music=new android.media.MediaPlayer();music.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());music.setDataSource(m.getAbsolutePath());music.setLooping(true);music.setVolume(.24f,.24f);music.prepare();syncMusic();}catch(Exception ignored){}});}catch(IOException ignored){}}).start(); for(int i=0;i<ids.length;i++)try{File f=new File(ctx.getCacheDir(),"rocket_sfx_"+i+".wav");write(f,i);ids[i]=pool.load(f.getAbsolutePath(),1);}catch(IOException ignored){}
 }
 private static void le(DataOutputStream d,int n,int bytes)throws IOException{for(int j=0;j<bytes;j++)d.writeByte(n>>(8*j));}
 private static void write(File f,int kind)throws IOException{
  int hz=22050;double[] lengths={.09,1,.5,.9,1.15,1.25,.25,.13,.25,.5};int count=(int)(hz*lengths[kind]);Random random=new Random(77+kind);double phase=0,noise=0;
  try(DataOutputStream d=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))){
   d.writeBytes("RIFF");le(d,36+count*2,4);d.writeBytes("WAVEfmt ");le(d,16,4);le(d,1,2);le(d,1,2);le(d,hz,4);le(d,hz*2,4);le(d,2,2);le(d,16,2);d.writeBytes("data");le(d,count*2,4);
   for(int j=0;j<count;j++){double t=j/(double)hz,u=j/(double)count,v=0;noise=.82*noise+.18*(random.nextDouble()*2-1);
    switch(kind){
     case 0: v=Math.sin(2*Math.PI*(850*t+1600*t*t))*Math.exp(-35*t);break;
     case 1: v=.32*Math.sin(2*Math.PI*48*t)+.18*Math.sin(2*Math.PI*96*t)+.65*noise+.15*(random.nextDouble()*2-1);v*=.85+.15*Math.sin(2*Math.PI*8*t);break;
     case 2: v=.65*noise+.18*Math.sin(2*Math.PI*110*t);break;
     case 3: v=(noise*2+.35*Math.sin(2*Math.PI*(90*t-35*t*t)))*Math.exp(-4*t);break;
     case 4: phase+=2*Math.PI*(650-530*u)/hz;v=(.65*Math.sin(phase)+.25*noise)*Math.sin(Math.PI*u);break;
     case 5: double[] notes={392,494,587,784,988};int k=Math.min(4,(int)(u*5));v=.55*Math.sin(2*Math.PI*notes[k]*t)*Math.sin(Math.PI*(u*5-k));break;
     case 6: phase+=2*Math.PI*(260+1100*u)/hz;v=Math.sin(phase)*Math.sin(Math.PI*u);break;
     case 7: v=Math.sin(2*Math.PI*660*t)*Math.sin(Math.PI*u);break;
     case 8: v=(Math.sin(2*Math.PI*1040*t)+.35*Math.sin(2*Math.PI*1560*t))*Math.sin(Math.PI*u);break;
     case 9: v=Math.sin(2*Math.PI*440*t)*Math.sin(Math.PI*u)*(.5+.5*Math.sin(2*Math.PI*8*t));break;
    }
    if(kind!=1&&kind!=2)v*=Math.min(1,j/100.0)*Math.min(1,(count-j)/100.0);
    le(d,(int)(Math.max(-1,Math.min(1,v)) * 23000),2);
   }
  }
 }
 void effect(int i){if(active&&ready[i])pool.play(ids[i],.65f,.65f,2,0,1f);}
 void update(boolean running,float speed,float thrust,boolean braking,int ship){
  if(!active||!running){stop();return;}
  float rate=Math.min(2f,.65f+.45f*speed+.65f*thrust+.025f*ship),vol=.07f+.12f*speed+.48f*thrust;
  if(engine==0&&ready[1])engine=pool.play(ids[1],vol,vol,1,-1,rate);
  if(engine!=0){pool.setRate(engine,rate);pool.setVolume(engine,vol,vol);}
  if(braking&&speed>.01f){if(brake==0&&ready[2])brake=pool.play(ids[2],.27f,.27f,1,-1,1f);}else if(brake!=0){pool.stop(brake);brake=0;}
 }
 void setMusic(boolean wanted){if(musicWanted!=wanted){musicWanted=wanted;syncMusic();}}
 private void syncMusic(){if(music!=null){if(active&&musicWanted){if(!music.isPlaying())music.start();}else if(music.isPlaying())music.pause();}}
 private static void writeMusic(File f)throws IOException{
 int hz=22050,beats=128;double beat=60.0/128;int count=(int)(hz*beats*beat);Random rng=new Random(913);int[] roots={45,41,48,43};int[] melody={0,7,12,7,3,7,10,7,0,7,12,15,10,7,3,7};
 try(DataOutputStream d=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))){
 d.writeBytes("RIFF");le(d,36+count*2,4);d.writeBytes("WAVEfmt ");le(d,16,4);le(d,1,2);le(d,1,2);le(d,hz,4);le(d,hz*2,4);le(d,2,2);le(d,16,2);d.writeBytes("data");le(d,count*2,4);
 for(int j=0;j<count;j++){double t=j/(double)hz,b=t/beat,part=b%1,step=b*2%1;int bar=(int)(b/4),root=roots[(bar/2)%4],minor=root==45||root==41?3:4;double bass=440*Math.pow(2,(root-12-69)/12.0),lead=440*Math.pow(2,(root+12+melody[(int)(b*2)%16]-69)/12.0);
 double kick=.34*Math.sin(2*Math.PI*(48*part*beat+5*(1-Math.exp(-part*12))))*Math.exp(-part*13);
 double hat=.07*(rng.nextDouble()*2-1)*Math.exp(-step*20);
 double snare=((int)b%4==1||(int)b%4==3)?.12*(rng.nextDouble()*2-1)*Math.exp(-part*13):0;
 double low=.19*(Math.sin(2*Math.PI*bass*t)+.25*Math.sin(4*Math.PI*bass*t))*Math.sin(Math.PI*step);
 double arp=.10*(Math.sin(2*Math.PI*lead*t)+.25*Math.sin(4*Math.PI*lead*t))*Math.sin(Math.PI*step)*Math.exp(-step*2);
 double pad=0;for(int interval:new int[]{0,minor,7}){double freq=440*Math.pow(2,(root+12+interval-69)/12.0);pad+=.035*Math.sin(2*Math.PI*freq*t)*Math.sin(Math.PI*(b%8)/8);}
 double v=(kick+hat+snare+low+arp+pad)*Math.min(1,Math.min(j,count-j)/220.0);le(d,(int)(Math.max(-1,Math.min(1,v))*26000),2);}
 }}
 void stop(){if(engine!=0)pool.stop(engine);if(brake!=0)pool.stop(brake);engine=brake=0;}
 void suspend(){active=false;stop();pool.autoPause();syncMusic();}
 void resume(){active=true;syncMusic();}
 void release(){released=true;stop();pool.release();if(music!=null){music.release();music=null;}}
}
