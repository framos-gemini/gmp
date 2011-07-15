package edu.gemini.aspen.gds.fits

import org.junit.Test
import org.junit.Assert._
import java.io.File
import edu.gemini.aspen.giapi.data.FitsKeyword


class FitsReaderTest {
    val names = Set("PODPSFF", "NAXIS", "FILTER1", "FILTNAM1", "DARKFILE", "CD2_1", "UCH2CJTM", "GAL_LONG", "STDCFFP", "STDCFFF", "FLATCORR", "ECL_LAT", "EXPFLAG", "MTFLAG", "DATALOST", "DATE-OBS", "STAC_I", "SKYSUB2", "SKYSUB1", "NAXIS2", "CD1_2", "PSTPTIME", "DARKTIME", "SHADFILE", "DARKDFIL", "SURFLONG", "OPUS_VER", "BIASFILE", "DETECTOR", "HISTWIDE", "SATURATE", "MEDIAN", "ATODGAIN", "MEANC100", "ERRCNT", "IRAF-TLM", "MEANC25", "EQRADTRG", "BLEVDFIL", "ECL_LONG", "EXTEND", "FGSLOCK", "OUTDTYPE", "SIMPLE", "CD2_2", "UEXPODUR", "GAL_LAT", "CRVAL1", "PSTRTIME", "MASKCORR", "NAXIS1", "COMMENT", "CD1_1", "MEANC10", "MODE", "FILTNAM2", "SOFTERRS", "NPRATRG", "DOHISTOS", "USCALE", "UZERO", "SKYSUB3", "EQNX_SUN", "DOSATMAP", "PYS_ITER", "NPDECTRG", "UCH1CJTM", "CRPIX2", "SURFLATD", "BACKGRND", "FILETYPE", "OBJECT", "READTIME", "LONGPMER", "PYS_VERS", "PEP_EXPO", "GRAPHTAB", "PHOTMODE", "BIASEVEN", "MASKFILE", "ATODSAT", "SKYSUB4", "PKTFMT", "LPKTTIME", "EXPTIME", "MEANC50", "STATICD", "TARGNAME", "ORIENTAT", "SUNANGLE", "DEZERO", "CALIBDEF", "UEXPOTIM", "RA_TARG", "LINENUM", "EPLONGPM", "PHOTTAB", "CRVAL2", "SEQNAME", "PA_V3", "CRPIX1", "SEQLINE", "SHADCORR", "DARKCORR", "INSTRUME", "PHOTBW", "ROOTNAME", "GOODMAX", "LRFWAVE", "NSHUTA17", "DOPHOTOM", "UCH4CJTM", "MEDSHADO", "IRAF_I", "BIASDFIL", "KSPOTS", "TELESCOP", "DEC_TARG", "MOONANGL", "EXPEND", "FILLCNT", "GPIXELS", "SURFALTD", "SERIALS", "UBAY3TMP", "RA_SUN", "GOODMIN", "FILTER2", "SKEWNESS", "FILTROT", "EQUINOX", "BIASCORR", "IMAGETYP", "SHUTTER", "PHOTZPT", "PHOTPLAM", "BIASODD", "SUN_ALT", "NCOMBINE", "TIME-OBS", "UCH3CJTM", "ORIGIN", "ATODFILE", "EXPSTART", "PHOTFLAM", "CTYPE1", "CAL_VER", "PROCTIME", "FLATDFIL", "BITPIX", "DATAMEAN", "RSDPFILL", "FPKTTIME", "PYS_HEXP", "MIR_REVR", "DEC_SUN", "BLEVCORR", "COMPTAB", "FLATFILE", "ROTRTTRG", "DATE", "BADPIXEL", "HISTORY", "FLATNTRG", "ATODCORR", "MEANC200", "BLEVFILE", "MEANC300", "CDBSFILE", "PROPOSID", "CTYPE2", "OVERLAP")
    val namesExt = Set("LINE", "TBCOL23", "TTYPE18", "NAXIS", "TBCOL25", "SAMPLE", "TFORM9", "TFORM24", "TBCOL10", "TTYPE24", "CARPOS", "TFORM16", "TBCOL5", "TTYPE21", "TBCOL15", "TBCOL24", "NAXIS2", "ZSCOEF3", "TBCOL16", "TTYPE11", "ERRCNT", "XTENSION", "GCOUNT", "TFORM11", "CRVAL1", "TTYPE6", "TTYPE19", "NAXIS1", "TBCOL14", "TFORM25", "TFIELDS", "TFORM1", "CD1_1", "TTYPE25", "TTYPE22", "TTYPE5", "TFORM7", "TBCOL19", "TFORM15", "TFORM21", "DELTAS", "ZSCOEF4", "DATAMAX", "EXTNAME", "TFORM12", "TBCOL22", "TTYPE16", "TBCOL2", "TBCOL6", "TTYPE15", "TBCOL18", "TFORM2", "TBCOL21", "TFORM6", "OBSRPT", "CRPIX1", "TTYPE23", "TBCOL12", "TBCOL8", "TFORM3", "EXPOSURE", "TBCOL13", "TTYPE7", "TTYPE4", "PCOUNT", "YDEF", "XDEF", "FILLCNT", "TFORM19", "TTYPE2", "PKTTIME", "TBCOL7", "TBCOL9", "TTYPE1", "TBCOL3", "BINID", "TFORM13", "TTYPE14", "TFORM23", "TTYPE20", "TFORM5", "OBSINT", "TBCOL11", "TTYPE9", "CTYPE1", "TBCOL17", "TBCOL20", "TBCOL1", "TTYPE17", "TFORM20", "DEC_APER", "TFORM17", "BITPIX", "TBCOL4", "TTYPE3", "ZSCOEF1", "ZSCOEF2", "TFORM22", "TTYPE13", "TFORM18", "TTYPE12", "RA_APER", "DATAMIN", "TFORM10", "TTYPE10", "TFORM4", "TTYPE8", "TFORM14", "TFORM8")

    @Test
    def testKeywords() {
        val fr = new FitsReader(new File(classOf[FitsReaderTest].getResource("S20110427-01.fits").toURI))

        assertEquals(names map {
            new FitsKeyword(_)
        }, fr.getKeywords())

    }

    @Test
    def testKeywordsExtension() {
        val fr = new FitsReader(new File(classOf[FitsReaderTest].getResource("FITS_WITH_EXTENSIONS.fits").toURI))
        assertEquals(namesExt map {
            new FitsKeyword(_)
        }, fr.getKeywords(1))

    }
}