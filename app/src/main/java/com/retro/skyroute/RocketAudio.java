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
 private boolean active=true;
 RocketAudio(Context ctx){
  pool=new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
  pool.setOnLoadCompleteListener((p,id,status)->{if(status==0)for(int i=0;i<ids.length;i++)if(ids[i]==id)ready[i]=true;});
  for(int i=0;i<ids.length;i++)try{File f=new File(ctx.getCacheDir(),"rocket_sfx_"+i+".wav");write(f,i);ids[i]=pool.load(f.getAbsolutePath(),1);}catch(IOException ignored){}
 }
 private static void le(DataOutputStream d,int n,int bytes)throws IOException{for(int j=0;j<bytes;j++)d.writeByte(n>>(8*j));}
 private static void write(File f,int kind)throws IOException{
  int hz=22050;double[] lengths={.09,1,.5,.9,1.15,1.25,.25,.13,.25,.5};int count=(int)(hz*lengths[kind]);Random random=new Random(77+kind);double phase=0,noise=0;
  try(DataOutputStream d=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))){
   d.writeBytes("RIFF");le(d,36+count*2,4);d.writeBytes("WAVEfmt ");le(d,16,4);le(d,1,2);le(d,1,2);le(d,hz,4);le(d,hz*2,4);le(d,2,2);le(d,16,2);d.writeBytes("data");le(d,count*2,4);
   for(int j=0;j<count;j++){double t=j/(double)hz,u=j/(double)count,v=0;noise=.82*noise+.18*(random.nextDouble()*2-1);
    switch(kind){
     case 0: v=Math.sin(2*Math.PI*(850*t+1600*t*t))*Math.exp(-35*t);break;
     case 1: v=.50*Math.sin(2*Math.PI*60*t)+.24*Math.sin(2*Math.PI*120*t)+.13*Math.sin(2*Math.PI*240*t)+.10*Math.sin(2*Math.PI*360*t);break;
     case 2: v=(.55*Math.sin(2*Math.PI*180*t)+.25*Math.sin(2*Math.PI*360*t))*(.8+.2*Math.sin(2*Math.PI*12*t));break;
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
  float rate=Math.min(2f,.65f+.75f*speed+.35f*thrust+.025f*ship),vol=.12f+.16f*speed+.20f*thrust;
  if(engine==0&&ready[1])engine=pool.play(ids[1],vol,vol,1,-1,rate);
  if(engine!=0){pool.setRate(engine,rate);pool.setVolume(engine,vol,vol);}
  if(braking&&speed>.01f){if(brake==0&&ready[2])brake=pool.play(ids[2],.27f,.27f,1,-1,1f);}else if(brake!=0){pool.stop(brake);brake=0;}
 }
 void stop(){if(engine!=0)pool.stop(engine);if(brake!=0)pool.stop(brake);engine=brake=0;}
 void suspend(){active=false;stop();pool.autoPause();}
 void resume(){active=true;}
 void release(){stop();pool.release();}
}
