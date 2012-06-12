/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package info.ineighborhood.cardme.vcard.types.media;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Mar 10, 2010
 *
 */
public enum AudioMediaType {

	/*
	 * IANA Registered Sound Types.
	 * Some may not have registered an extension.
	 */
	
	GPP3("GPP3", "aduio/3gpp", ""),
	GPP2("GPP2", "aduio/3gpp2", ""),
	AC3("AC3", "aduio/ac3", "ac3"),
	AMR("AMR", "aduio/amr", "amr"),
	AMR_WB("AMR_WB", "aduio/amr-wb", "amr-wb"),
	AMR_WB_PLUS("AMR_WB_PLUS", "aduio/amr-wb+", "amr-wb+"),
	ASC("ASC", "aduio/asc", ""),
	ATRAC_ADVANCED_LOSSLESS("ATRAC_ADVANCED_LOSSLESS", "aduio/atrac-advanced-lossless", ""),
	ATRAC_X("ATRAC_X", "aduio/atrac-x", ""),
	ATRAC3("ATRAC3", "aduio/atrac3", ""),
	BASIC("BASIC", "aduio/basic", ""),
	BV16("BV16", "aduio/bv16", ""),
	BV32("BV32", "aduio/bv32", ""),
	CLEARMODE("CLEARMODE", "aduio/clearmode", ""),
	CN("CN", "aduio/cn", ""),
	DAT12("DAT12", "aduio/dat12", ""),
	DLS("DLS", "aduio/dls", ""),
	DSR_ES201108("DSR_ES201108", "aduio/dsr-es201108", ""),
	DSR_ES202050("DSR_ES202050", "aduio/dsr-es202050", ""),
	DSR_ES202211("DSR_ES202211", "aduio/dsr-es202211", ""),
	DSR_ES202212("DSR_ES202212", "aduio/dsr-es202212", ""),
	EAC3("EAC3", "aduio/eac3", "eac3"),
	DVI4("DVI4", "aduio/dvi4", ""),
	EVRC("EVRC", "aduio/evrc", ""),
	EVRC0("EVRC0", "aduio/evrc0", ""),
	EVRC1("EVRC1", "aduio/evrc1", ""),
	EVRCB("EVRCB", "aduio/evrcb", ""),
	EVRCB0("EVRCB0", "aduio/evrcb0", ""),
	EVRCB1("EVRCB1", "aduio/evrcb1", ""),
	EVRC_QCP("EVRC_QCP", "aduio/evrc-qcp", ""),
	EVRCWB("EVRCWB", "aduio/evrcwb", ""),
	EVRCWB0("EVRCWB0", "aduio/evrcwb0", ""),
	EVRCWB1("EVRCWB1", "aduio/evrcwb1", ""),
	G719("G719", "aduio/g719", ""),
	G722("G722", "aduio/g722", ""),
	G7221("G7221", "aduio/g7221", ""),
	G723("G723", "aduio/g723", ""),
	G726_16("G726_16", "aduio/g726-16", ""),
	G726_24("G726_24", "aduio/g726-24", ""),
	G726_32("G726_32", "aduio/g726-32", ""),
	G726_40("G726_40", "aduio/g726-40", ""),
	G728("G728", "aduio/g728", ""),
	G729("G729", "aduio/g729", ""),
	G7291("G7291", "aduio/g7291", ""),
	G729D("G729D", "aduio/g729d", ""),
	G729E("G729E", "aduio/g729e", ""),
	GSM("GSM", "aduio/gsm", ""),
	GSM_EFR("GSM_EFR", "aduio/gsm-efr", ""),
	ILBC("ILBC", "aduio/ilbc", ""),
	L8("L8", "aduio/l8", ""),
	L16("L16", "aduio/l16", ""),
	L20("L20", "aduio/l20", ""),
	L24("L24", "aduio/l24", ""),
	LPC("LPC", "aduio/lpc", ""),
	MOBILE_XMF("MOBILE_XMF", "aduio/mobile-xmf", ""),
	MPA("MPA", "aduio/mpa", "mpa"),
	MP4("MP4", "aduio/mp4", "mp4"),
	MP4A_LATM("MP$_LATM", "aduio/mp4-latm", ""),
	MPA_ROBUST("MPA_ROBUST", "aduio/mpa-robust", ""),
	MPEG("MPEG", "aduio/mpeg", "mpeg"),
	MPEG4_GENERIC("MPEG4_GENERIC", "aduio/mpeg4-generic", "mpeg"),
	OGG("OGG", "aduio/ogg", "ogg"),
	PARITYFEC_1D_INT("PARITYFEC_1D_INT", "audio/1d-interleaved-parityfec", ""),
	PARITYFEC("PARITYFEC", "aduio/parityfec", ""),
	PCMA("PCMA", "aduio/pcma", ""),
	PCMA_WB("PCMA_WB", "aduio/pcma-wb", ""),
	PCMU("PCMU", "aduio/pcmu", ""),
	PCMU_WB("PCMU_WB", "aduio/pcmu-wb", ""),
	PRS_SID("PRS_SID", "aduio/prs.sid", "sid"),
	QCELP("QCELP", "aduio/qcelp", ""),
	RED("RED", "aduio/red", ""),
	RTP_MIDI("RTP_MIDI", "aduio/rtp-midi", ""),
	RTX("RTX", "aduio/rtx", ""),
	SMV("SMV", "aduio/smv", ""),
	SMV0("SMV0", "aduio/smv0", ""),
	SMV_QCP("SMV_QCP", "aduio/smv-qcp", ""),
	SPEEX("SPEEX", "aduio/speex", ""),
	T140C("T140C", "aduio/t140c", ""),
	T38("T38", "aduio/t38", ""),
	TELEPHONE_EVENT("TELEPHONE_EVENT", "aduio/telephone-event", ""),
	TONE("TONE", "aduio/tone", ""),
	UEMCLIP("UEMCLIP", "aduio/uemclip", ""),
	ULPFEC("ULPFEC", "aduio/ulpfec", ""),
	VDVI("VDVI", "aduio/vdvi", ""),
	VMR_WB("VMR_WB", "aduio/vmr-wb", ""),
	VORBIS("VORBIS", "aduio/vorbis", ""),
	VORBIS_CONFIG("VORBIS_CONFIG", "aduio/vorbis-config", ""),
	RTP_ENC_AESCM128("RTP_ENC_AESCM128", "aduio/rtp-enc-aescm128", ""),
	SP_MIDI("SP_MIDI", "aduio/sp-midi ", "mid"),
	GPP3_IUFP("GPP3_IUFP", "aduio/vnd.3gpp.iufp", ""),
	SB4("SB4", "aduio/vnd.4sb", ""),
	AUDIOKOZ("AUDIOKOZ", "aduio/vnd.audiokoz", "koz"),
	CELP("CELP", "aduio/vnd.CELP", ""),
	NSE("NSE", "aduio/vnd.cisco.com", ""),
	CMLES_RADIO_EVENTS("CMLES_RADIO_EVENTS", "aduio/vnd.cmles.radio-events", ""),
	CNS_ANP1("CNS_ANP1", "aduio/vnd.cns.anp1", ""),
	CND_INF1("CNS_INF1", "aduio/vnd.cns.inf1", ""),
	EOL("EOL", "aduio/vnd.digital-winds", "eol"),
	DLNA_ADTS("DLNA_ADTS", "aduio/vnd.dlna.adts", ""),
	HEAAC1("HEAAC1", "aduio/vnd.dolby.heaac.1", ""),
	HEAAC2("HEAAC2", "aduio/vnd.dolby.heaac.2", ""),
	MPL("MPL", "aduio/vnd.dolby.mlp", "mpl"),
	MPS("MPS", "aduio/vnd.dolby.mps", ""),
	PL2("PL2", "aduio/vnd.dolby.pl2", ""),
	PL2X("PL2X", "aduio/vnd.dolby.pl2x", ""),
	PL2Z("PL2Z", "aduio/vnd.dolby.pl2z", ""),
	PULSE_1("PULSE_1", "aduio/vnd.dolby.pulse.1", ""),
	DRA("DRA", "aduio/vnd.dra", ""),
	DTS("DTS", "aduio/vnd.dts", "WAV"),				//wav, cpt, dts
	DTSHD("DTSHD", "aduio/vnd.dts.hd", "dtshd"),
	PLJ("PLJ", "aduio/vnd.everad.plj", "plj"),
	AUDIO("AUDIO", "aduio/vnd.hns.audio", "rm"),
	VOICE("LVP", "aduio/vnd.lucent.voice", "lvp"),
	PYA("PYA", "aduio/vnd.ms-playready.media.pya", "pya"),
	MXMF("MXMF", "aduio/vnd.nokia.mobile-xmf", "mxmf"),
	VBK("VBK", "aduio/vnd.nortel.vbk", "vbk"),
	ECELP4800("ECELP4800", "aduio/vnd.nuera.ecelp4800", "ecelp4800"),
	ECELP7470("ECELP7470", "aduio/vnd.nuera.ecelp7470", "ecelp7470"),
	ECELP9600("ECELP9600", "aduio/vnd.nuera.ecelp9600", "ecelp9600"),
	SBC("SBC", "aduio/vnd.octel.sbc", ""),
	KADPCM32("KADPCM32", "aduio/vnd.rhetorex.32kadpcm", ""),
	SMP3("SMP3", "aduio/vnd.sealedmedia.softseal.mpeg", "smp3"),	//smp3, smp, s1m
	CVSD("CVSD", "aduio/vnd.vmx.cvsd", ""),
	NON_STANDARD("NON_STANDARD","","");
	
	private String typeName;
	private String ianaRegisteredName;
	private String extension;
	AudioMediaType(String _typeName, String _ianaRegisteredName, String _extension) {
		typeName = _typeName;
		ianaRegisteredName = _ianaRegisteredName;
		extension = _extension;
	}
	
	public String getTypeName()
	{
		return typeName;
	}
	
	public String getIanaRegisteredName()
	{
		return ianaRegisteredName;
	}
	
	public String getExtension()
	{
		return extension;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public void setIanaRegisteredName(String ianaRegisteredName) {
		this.ianaRegisteredName = ianaRegisteredName;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	@Override
	public String toString()
	{

		return typeName;
	}
}
