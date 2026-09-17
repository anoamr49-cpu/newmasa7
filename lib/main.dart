import 'dart:convert';
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:share_plus/share_plus.dart';
import 'package:geolocator/geolocator.dart';

void main() => runApp(const AmrToolsApp());

class AmrToolsApp extends StatefulWidget {
  const AmrToolsApp({super.key});
  @override State<AmrToolsApp> createState() => _AmrToolsAppState();
}

class _AmrToolsAppState extends State<AmrToolsApp> {
  ThemeMode mode = ThemeMode.system;
  void toggleTheme() => setState(() {
    mode = mode == ThemeMode.dark ? ThemeMode.light : ThemeMode.dark;
  });
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'مساح ومقاول',
    themeMode: mode,
    theme: ThemeData(
      useMaterial3: true,
      fontFamily: 'Arial',
      colorSchemeSeed: const Color(0xFF087F78),
      brightness: Brightness.light,
    ),
    darkTheme: ThemeData(
      useMaterial3: true,
      fontFamily: 'Arial',
      colorSchemeSeed: const Color(0xFF20B2AA),
      brightness: Brightness.dark,
    ),
    home: HomeScreen(onTheme: toggleTheme),
  );
}

class HomeScreen extends StatefulWidget {
  final VoidCallback onTheme;
  const HomeScreen({super.key, required this.onTheme});
  @override State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int index = 0;
  final pages = const [Dashboard(), CalculatorPage(), ToolsPage(), AboutPage()];
  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(child: pages[index]),
    bottomNavigationBar: NavigationBar(
      selectedIndex: index,
      onDestinationSelected: (v) => setState(() => index = v),
      destinations: const [
        NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'الرئيسية'),
        NavigationDestination(icon: Icon(Icons.calculate_outlined), selectedIcon: Icon(Icons.calculate), label: 'الحاسبات'),
        NavigationDestination(icon: Icon(Icons.straighten_outlined), selectedIcon: Icon(Icons.straighten), label: 'الأدوات'),
        NavigationDestination(icon: Icon(Icons.info_outline), selectedIcon: Icon(Icons.info), label: 'عن التطبيق'),
      ],
    ),
  );
}

class Dashboard extends StatelessWidget {
  const Dashboard({super.key});
  @override
  Widget build(BuildContext context) => CustomScrollView(
    slivers: [
      SliverToBoxAdapter(child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 24, 20, 8),
        child: Row(children: [
          CircleAvatar(
            radius: 28,
            backgroundColor: Theme.of(context).colorScheme.primary,
            child: const Icon(Icons.architecture, color: Colors.white, size: 30),
          ),
          const SizedBox(width: 12),
          const Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('مساح ومقاول', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
            Text('منصة المساحة والحصر الرقمية', style: TextStyle(fontSize: 13)),
          ])),
          IconButton(onPressed: () => showAboutDialog(
            context: context, applicationName: 'مساح ومقاول',
            applicationVersion: '1.0.0',
            children: const [Text('أدوات هندسية ومساحية للحسابات السريعة بالموقع.')],
          ), icon: const Icon(Icons.more_vert)),
        ]),
      )),
      SliverToBoxAdapter(child: Padding(
        padding: const EdgeInsets.all(20),
        child: Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(28),
            gradient: LinearGradient(colors: [
              Theme.of(context).colorScheme.primary,
              Theme.of(context).colorScheme.secondary,
            ]),
          ),
          child: const Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('حاسب الكميات بسرعة ⚡', style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
            SizedBox(height: 8),
            Text('احسب الخرسانة، المباني، المحارة، السيراميك، الحديد والميول من مكان واحد.', style: TextStyle(color: Colors.white70, height: 1.5)),
          ]),
        ),
      )),
      SliverPadding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        sliver: SliverGrid.count(
          crossAxisCount: 2, crossAxisSpacing: 12, mainAxisSpacing: 12, childAspectRatio: 1.15,
          children: const [
            ToolCard(icon: Icons.home_work_outlined, title: 'أوضة / صالة', sub: 'مباني وتشطيبات', page: 1),
            ToolCard(icon: Icons.view_in_ar_outlined, title: 'خرسانة', sub: 'تكعيب العناصر', page: 1),
            ToolCard(icon: Icons.grid_4x4, title: 'حديد', sub: 'وزن نظري', page: 1),
            ToolCard(icon: Icons.straighten, title: 'الميل', sub: 'فرق المناسيب', page: 2),
          ],
        ),
      ),
      const SliverToBoxAdapter(child: Padding(
        padding: EdgeInsets.all(20),
        child: Text('القوانين الأساسية', style: TextStyle(fontSize: 19, fontWeight: FontWeight.bold)),
      )),
      SliverToBoxAdapter(child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
        child: Column(children: const [
          FormulaCard('الخرسانة والحفر', 'الحجم = الطول × العرض × الارتفاع'),
          FormulaCard('وزن الحديد', 'kg/m = القطر² ÷ 162'),
          FormulaCard('المحارة والسيراميك', 'المساحة = (المحيط × الارتفاع) − الفتحات + الهالك'),
        ]),
      )),
    ],
  );
}

class ToolCard extends StatelessWidget {
  final IconData icon; final String title, sub; final int page;
  const ToolCard({super.key, required this.icon, required this.title, required this.sub, required this.page});
  @override Widget build(BuildContext context) => InkWell(
    borderRadius: BorderRadius.circular(22),
    onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => page == 1 ? const CalculatorPage() : const ToolsPage())),
    child: Card(
      elevation: 0,
      child: Padding(padding: const EdgeInsets.all(16), child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 30, color: Theme.of(context).colorScheme.primary),
          const Spacer(),
          Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          Text(sub, style: Theme.of(context).textTheme.bodySmall),
        ],
      )),
    ),
  );
}
class FormulaCard extends StatelessWidget {
  final String title, formula;
  const FormulaCard(this.title, this.formula, {super.key});
  @override Widget build(BuildContext context) => Card(
    child: ListTile(leading: const Icon(Icons.functions), title: Text(title), subtitle: Text(formula)),
  );
}

class CalculatorPage extends StatelessWidget {
  const CalculatorPage({super.key});
  @override Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('الحاسبات الهندسية')),
    body: ListView(padding: const EdgeInsets.all(16), children: [
      CalcTile(icon: Icons.home_work_outlined, title: 'حسبة أوضة أو صالة', subtitle: 'مباني + خرسانة + محارة + تشطيبات', onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RoomCalculator()))),
      CalcTile(icon: Icons.fence, title: 'حسبة سور بالبلوك الأبيض', subtitle: 'عدد البلوك والمونة بشكل إرشادي', onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const WallCalculator()))),
      CalcTile(icon: Icons.format_paint, title: 'محارة واجهات', subtitle: 'مساحة صافية + نسبة هالك', onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PlasterCalculator()))),
      CalcTile(icon: Icons.construction, title: 'وزن الحديد', subtitle: 'الوزن النظري للقطر والطول', onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const SteelCalculator()))),
    ]),
  );
}
class CalcTile extends StatelessWidget {
  final IconData icon; final String title, subtitle; final VoidCallback onTap;
  const CalcTile({super.key, required this.icon, required this.title, required this.subtitle, required this.onTap});
  @override Widget build(BuildContext context) => Card(
    margin: const EdgeInsets.only(bottom: 12),
    child: ListTile(
      contentPadding: const EdgeInsets.all(14),
      leading: CircleAvatar(child: Icon(icon)),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
      subtitle: Text(subtitle),
      trailing: const Icon(Icons.chevron_left),
      onTap: onTap,
    ),
  );
}

class NumField extends StatelessWidget {
  final TextEditingController controller; final String label; final String? suffix;
  const NumField({super.key, required this.controller, required this.label, this.suffix});
  @override Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 12),
    child: TextField(
      controller: controller,
      keyboardType: const TextInputType.numberWithOptions(decimal: true),
      decoration: InputDecoration(labelText: label, suffixText: suffix, border: const OutlineInputBorder()),
    ),
  );
}
double n(TextEditingController c) => double.tryParse(c.text.replaceAll(',', '.')) ?? 0;

class RoomCalculator extends StatefulWidget {
  const RoomCalculator({super.key});
  @override State<RoomCalculator> createState() => _RoomCalculatorState();
}
class _RoomCalculatorState extends State<RoomCalculator> {
  final l=TextEditingController(), w=TextEditingController(), h=TextEditingController(), openings=TextEditingController();
  String result='';
  void calc() {
    final L=n(l), W=n(w), H=n(h), O=n(openings);
    final perimeter=2*(L+W);
    final wallGross=perimeter*H;
    final wallNet=math.max(0, wallGross-O);
    final concrete=L*W*0.12;
    final plaster=wallNet;
    setState(() => result =
      'مساحة الأرضية: ${(L*W).toStringAsFixed(2)} م²\n'
      'محيط الغرفة: ${perimeter.toStringAsFixed(2)} م\n'
      'حجم خرسانة سقف افتراضي 12 سم: ${concrete.toStringAsFixed(3)} م³\n'
      'مساحة المباني الصافية: ${wallNet.toStringAsFixed(2)} م²\n'
      'مساحة المحارة التقريبية: ${plaster.toStringAsFixed(2)} م²');
    saveHistory('أوضة/صالة', result);
  }
  @override Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('حسبة أوضة / صالة')),
    body: ListView(padding: const EdgeInsets.all(16), children: [
      NumField(controller:l,label:'الطول',suffix:'م'),
      NumField(controller:w,label:'العرض',suffix:'م'),
      NumField(controller:h,label:'ارتفاع السقف',suffix:'م'),
      NumField(controller:openings,label:'إجمالي الفتحات التقريبية',suffix:'م²'),
      FilledButton.icon(onPressed:calc, icon:const Icon(Icons.calculate), label:const Text('احسب الآن')),
      if(result.isNotEmpty) ResultBox(text:result),
    ]),
  );
}

class WallCalculator extends StatefulWidget {
  const WallCalculator({super.key});
  @override State<WallCalculator> createState() => _WallCalculatorState();
}
class _WallCalculatorState extends State<WallCalculator> {
  final L=TextEditingController(), H=TextEditingController();
  String result='';
  void calc() {
    final area=n(L)*n(H);
    const blockFace=0.4*0.2;
    final blocks=area/blockFace*1.05;
    setState(()=>result='مساحة السور: ${area.toStringAsFixed(2)} م²\nعدد البلوك التقريبي: ${blocks.ceil()} قطعة\nالهالك المحسوب: 5%');
  }
  @override Widget build(BuildContext context)=>Scaffold(
    appBar:AppBar(title:const Text('سور بالبلوك الأبيض')),
    body:ListView(padding:const EdgeInsets.all(16),children:[
      NumField(controller:L,label:'طول السور',suffix:'م'),
      NumField(controller:H,label:'ارتفاع السور',suffix:'م'),
      FilledButton(onPressed:calc,child:const Text('احسب')),
      if(result.isNotEmpty)ResultBox(text:result),
    ]),
  );
}

class PlasterCalculator extends StatefulWidget {
  const PlasterCalculator({super.key});
  @override State<PlasterCalculator> createState()=>_PlasterCalculatorState();
}
class _PlasterCalculatorState extends State<PlasterCalculator>{
  final area=TextEditingController(), openings=TextEditingController(), waste=TextEditingController(text:'10');
  String result='';
  void calc(){
    final a=n(area),o=n(openings),p=n(waste)/100;
    final net=math.max(0,a-o); final total=net*(1+p);
    setState(()=>result='المساحة الصافية: ${net.toStringAsFixed(2)} م²\nبعد إضافة الهالك: ${total.toStringAsFixed(2)} م²');
  }
  @override Widget build(BuildContext context)=>Scaffold(
    appBar:AppBar(title:const Text('محارة الواجهات')),
    body:ListView(padding:const EdgeInsets.all(16),children:[
      NumField(controller:area,label:'إجمالي المساحة',suffix:'م²'),
      NumField(controller:openings,label:'الفتحات',suffix:'م²'),
      NumField(controller:waste,label:'الهالك',suffix:'%'),
      FilledButton(onPressed:calc,child:const Text('احسب')),
      if(result.isNotEmpty)ResultBox(text:result),
    ]),
  );
}

class SteelCalculator extends StatefulWidget {
  const SteelCalculator({super.key});
  @override State<SteelCalculator> createState()=>_SteelCalculatorState();
}
class _SteelCalculatorState extends State<SteelCalculator>{
  final d=TextEditingController(), len=TextEditingController();
  String result='';
  void calc(){
    final dia=n(d), meters=n(len);
    final kgm=dia*dia/162; final kg=kgm*meters;
    setState(()=>result='وزن المتر: ${kgm.toStringAsFixed(3)} كجم/م\nالوزن الإجمالي: ${kg.toStringAsFixed(2)} كجم');
  }
  @override Widget build(BuildContext context)=>Scaffold(
    appBar:AppBar(title:const Text('وزن حديد التسليح')),
    body:ListView(padding:const EdgeInsets.all(16),children:[
      NumField(controller:d,label:'قطر السيخ',suffix:'مم'),
      NumField(controller:len,label:'إجمالي الطول',suffix:'م'),
      const Text('القانون: القطر² ÷ 162',style:TextStyle(fontWeight:FontWeight.bold)),
      const SizedBox(height:12),
      FilledButton(onPressed:calc,child:const Text('احسب')),
      if(result.isNotEmpty)ResultBox(text:result),
    ]),
  );
}

class ResultBox extends StatelessWidget {
  final String text;
  const ResultBox({super.key,required this.text});
  @override Widget build(BuildContext context)=>Card(
    margin:const EdgeInsets.only(top:18),
    child:Padding(padding:const EdgeInsets.all(18),child:Column(
      crossAxisAlignment:CrossAxisAlignment.start,
      children:[
        Row(children:[Icon(Icons.task_alt,color:Theme.of(context).colorScheme.primary),const SizedBox(width:8),const Text('النتيجة',style:TextStyle(fontSize:18,fontWeight:FontWeight.bold))]),
        const Divider(),
        SelectableText(text,style:const TextStyle(height:1.8)),
        const SizedBox(height:8),
        Align(alignment:AlignmentDirectional.centerEnd,child:TextButton.icon(
          onPressed:()=>SharePlus.instance.share(ShareParams(text:'مساح ومقاول\n$text')),
          icon:const Icon(Icons.share),label:const Text('مشاركة'))),
      ],
    )),
  );
}

Future<void> saveHistory(String title,String result) async {
  final p=await SharedPreferences.getInstance();
  final list=p.getStringList('history')??[];
  list.insert(0,jsonEncode({'title':title,'result':result,'date':DateTime.now().toIso8601String()}));
  await p.setStringList('history',list.take(20).toList());
}

class ToolsPage extends StatelessWidget {
  const ToolsPage({super.key});
  @override Widget build(BuildContext context)=>Scaffold(
    appBar:AppBar(title:const Text('الأدوات الهندسية')),
    body:ListView(padding:const EdgeInsets.all(16),children:[
      const SlopeTool(),
      const SizedBox(height:14),
      const GpsSurveyTool(),
      const SizedBox(height:14),
      const AiAssistantTool(),
      const SizedBox(height:14),
      const UnitTool(),
      const SizedBox(height:14),
      Card(child:ListTile(
        leading:const Icon(Icons.history),
        title:const Text('سجل الحسابات'),
        subtitle:const Text('آخر 20 عملية محفوظة على الجهاز'),
        onTap:()=>showHistory(context),
      )),
    ]),
  );
}
class SlopeTool extends StatefulWidget {
  const SlopeTool({super.key});
  @override State<SlopeTool> createState()=>_SlopeToolState();
}
class _SlopeToolState extends State<SlopeTool>{
  final a=TextEditingController(),b=TextEditingController(),dist=TextEditingController();
  String r='';
  void calc(){
    final diff=n(b)-n(a), d=n(dist);
    final pct=d==0?0:(diff/d*100), angle=d==0?0:math.atan2(diff,d)*180/math.pi;
    setState(()=>r='فرق المنسوب: ${diff.toStringAsFixed(3)} م\nالميل: ${pct.toStringAsFixed(2)} %\nالزاوية: ${angle.toStringAsFixed(3)}°');
  }
  @override Widget build(BuildContext context)=>Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(
    crossAxisAlignment:CrossAxisAlignment.start,children:[
      const Text('محاكي الميول والقطاعات',style:TextStyle(fontSize:18,fontWeight:FontWeight.bold)),
      const SizedBox(height:12),
      NumField(controller:a,label:'منسوب النقطة A',suffix:'م'),
      NumField(controller:b,label:'منسوب النقطة B',suffix:'م'),
      NumField(controller:dist,label:'المسافة الأفقية',suffix:'م'),
      FilledButton(onPressed:calc,child:const Text('تشغيل المحاكاة')),
      if(r.isNotEmpty)Padding(padding:const EdgeInsets.only(top:12),child:Text(r,style:const TextStyle(height:1.8))),
    ],
  )));
}
class UnitTool extends StatefulWidget {
  const UnitTool({super.key});
  @override State<UnitTool> createState()=>_UnitToolState();
}
class _UnitToolState extends State<UnitTool>{
  final c=TextEditingController();
  String unit='متر → قدم',r='';
  void calc(){
    final x=n(c);
    final v=switch(unit){'متر → قدم'=>x*3.280839895,'قدم → متر'=>x/3.280839895,'م² → قدم²'=>x*10.7639104167,'قدم² → م²'=>x/10.7639104167,_=>x};
    setState(()=>r=v.toStringAsFixed(6));
  }
  @override Widget build(BuildContext context)=>Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(
    crossAxisAlignment:CrossAxisAlignment.start,children:[
      const Text('محول الوحدات',style:TextStyle(fontSize:18,fontWeight:FontWeight.bold)),
      const SizedBox(height:12), NumField(controller:c,label:'القيمة'),
      DropdownButtonFormField<String>(
        initialValue:unit,decoration:const InputDecoration(border:OutlineInputBorder(),labelText:'نوع التحويل'),
        items:['متر → قدم','قدم → متر','م² → قدم²','قدم² → م²'].map((e)=>DropdownMenuItem(value:e,child:Text(e))).toList(),
        onChanged:(v)=>setState(()=>unit=v!)),
      const SizedBox(height:12),FilledButton(onPressed:calc,child:const Text('حوّل')),
      if(r.isNotEmpty)Text('النتيجة: $r',style:const TextStyle(fontWeight:FontWeight.bold)),
    ],
  )));
}


class GpsSurveyTool extends StatefulWidget {
  const GpsSurveyTool({super.key});
  @override State<GpsSurveyTool> createState()=>_GpsSurveyToolState();
}
class _GpsSurveyToolState extends State<GpsSurveyTool>{
  final List<_SurveyPoint> points=[];
  String status='لم يتم تحديد الموقع بعد';
  double? lat, lon, accuracy;

  Future<void> getPosition() async {
    try {
      final service = await Geolocator.isLocationServiceEnabled();
      if(!service){ setState(()=>status='فعّل GPS من إعدادات الهاتف أولاً'); return; }
      var permission = await Geolocator.checkPermission();
      if(permission == LocationPermission.denied) permission = await Geolocator.requestPermission();
      if(permission == LocationPermission.denied || permission == LocationPermission.deniedForever){
        setState(()=>status='صلاحية الموقع مرفوضة'); return;
      }
      final p = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(accuracy: LocationAccuracy.bestForNavigation),
      );
      setState(() { lat=p.latitude; lon=p.longitude; accuracy=p.accuracy; status='تم تحديد الموقع'; });
    } catch(e) {
      setState(()=>status='تعذر تحديد الموقع: $e');
    }
  }

  void addPoint(){
    if(lat==null || lon==null) return;
    setState(()=>points.add(_SurveyPoint(lat!,lon!,'P${points.length+1}')));
  }

  double distance(_SurveyPoint a,_SurveyPoint b){
    const r=6378137.0;
    final p1=a.lat*math.pi/180, p2=b.lat*math.pi/180;
    final dp=(b.lat-a.lat)*math.pi/180, dl=(b.lon-a.lon)*math.pi/180;
    final h=math.sin(dp/2)*math.sin(dp/2)+math.cos(p1)*math.cos(p2)*math.sin(dl/2)*math.sin(dl/2);
    return 2*r*math.asin(math.min(1,math.sqrt(h)));
  }

  double polygonArea(){
    if(points.length<3) return 0;
    const r=6378137.0;
    final lat0=points.map((p)=>p.lat).reduce((a,b)=>a+b)/points.length*math.pi/180;
    final xs=points.map((p)=>r*p.lon*math.pi/180*math.cos(lat0)).toList();
    final ys=points.map((p)=>r*p.lat*math.pi/180).toList();
    double s=0;
    for(int i=0;i<points.length;i++){
      final j=(i+1)%points.length;
      s += xs[i]*ys[j]-xs[j]*ys[i];
    }
    return s.abs()/2;
  }

  @override Widget build(BuildContext context)=>Card(child:Padding(
    padding:const EdgeInsets.all(16), child:Column(
      crossAxisAlignment:CrossAxisAlignment.start, children:[
        const Text('المساحة GPS والرفع الميداني',style:TextStyle(fontSize:18,fontWeight:FontWeight.bold)),
        const SizedBox(height:8),
        Text(status),
        if(lat!=null) Text('Lat: ${lat!.toStringAsFixed(8)}\nLon: ${lon!.toStringAsFixed(8)}\nالدقة التقريبية: ${accuracy!.toStringAsFixed(1)} م'),
        const SizedBox(height:10),
        Wrap(spacing:8,runSpacing:8,children:[
          FilledButton.icon(onPressed:getPosition,icon:const Icon(Icons.my_location),label:const Text('تحديد موقعي')),
          OutlinedButton.icon(onPressed:addPoint,icon:const Icon(Icons.add_location_alt),label:const Text('حفظ نقطة')),
        ]),
        const SizedBox(height:8),
        Text('النقاط المحفوظة: ${points.length}'),
        if(points.length>=2) Text('آخر مسافة: ${distance(points[points.length-2],points.last).toStringAsFixed(3)} م'),
        if(points.length>=3) Text('مساحة المضلع التقريبية: ${polygonArea().toStringAsFixed(3)} م²'),
        if(points.isNotEmpty) ...points.map((p)=>ListTile(
          dense:true, leading:const Icon(Icons.place), title:Text(p.name),
          subtitle:Text('${p.lat.toStringAsFixed(8)}, ${p.lon.toStringAsFixed(8)}'),
        )),
        const Text('تنبيه: GPS الهاتف مناسب للأعمال المبدئية والحصر العام، وليس بديلاً عن GNSS RTK أو Total Station في التوقيع الدقيق.',style:TextStyle(fontSize:11)),
      ],
    ),
  ));
}
class _SurveyPoint {
  final double lat,lon; final String name;
  _SurveyPoint(this.lat,this.lon,this.name);
}

class AiAssistantTool extends StatefulWidget {
  const AiAssistantTool({super.key});
  @override State<AiAssistantTool> createState()=>_AiAssistantToolState();
}
class _AiAssistantToolState extends State<AiAssistantTool>{
  final q=TextEditingController();
  String answer='اكتب سؤالك الهندسي، وسأساعدك في تحديد الحاسبة والقانون المناسب.';
  void ask(){
    final s=q.text.trim().toLowerCase();
    String a;
    if(s.contains('حديد') || s.contains('تسليح')){
      a='لوزن الحديد النظري: وزن المتر = القطر² ÷ 162. أدخل القطر بالمليمتر والطول بالمتر. للمشروع التنفيذي راجع اللوحات والمواصفات.';
    } else if(s.contains('خرسانة') || s.contains('تكعيب')){
      a='الحجم الأساسي = الطول × العرض × الارتفاع. عند وجود فتحات أو تقاطعات وعناصر متداخلة يجب خصم/فصل الأحجام حسب اللوحات التنفيذية.';
    } else if(s.contains('محارة') || s.contains('بياض')){
      a='المساحة الصافية = المساحة الإجمالية − الفتحات، ثم تُضاف نسبة الهالك وفق طبيعة التنفيذ. لا تعتمد نسبة ثابتة لكل المواقع.';
    } else if(s.contains('ميل') || s.contains('منسوب')){
      a='الميل % = فرق المنسوب ÷ المسافة الأفقية × 100. الزاوية = atan2(فرق المنسوب, المسافة الأفقية).';
    } else if(s.contains('gps') || s.contains('مساحة')){
      a='في الرفع بالهاتف يمكن تسجيل نقاط GPS وحساب المسافات والمساحة تقريبياً. للأعمال المساحية الحرجة استخدم RTK/Total Station ونظام إحداثيات المشروع.';
    } else {
      a='أقدر مساعدتك في الحصر، الخرسانة، الحديد، المباني، المحارة، الميل، الوحدات وأعمال المساحة. جرّب كتابة أبعاد المسألة وسأحدد طريقة الحساب.';
    }
    setState(()=>answer=a);
  }
  @override Widget build(BuildContext context)=>Card(child:Padding(
    padding:const EdgeInsets.all(16),child:Column(
      crossAxisAlignment:CrossAxisAlignment.start,children:[
        const Text('مساعد مساح ومقاول',style:TextStyle(fontSize:18,fontWeight:FontWeight.bold)),
        const SizedBox(height:6),
        const Text('مساعد حسابي داخل التطبيق. يمكن لاحقاً ربطه بخدمة AI خارجية عبر API آمن دون وضع المفتاح داخل التطبيق.'),
        const SizedBox(height:12),
        TextField(controller:q,maxLines:3,decoration:const InputDecoration(
          labelText:'اسأل عن مسألة أو اكتب الأبعاد',border:OutlineInputBorder())),
        const SizedBox(height:10),
        FilledButton.icon(onPressed:ask,icon:const Icon(Icons.auto_awesome),label:const Text('حلّل السؤال')),
        const SizedBox(height:10),
        SelectableText(answer,style:const TextStyle(height:1.7)),
      ],
    ),
  ));
}

Future<void> showHistory(BuildContext context) async {
  final p=await SharedPreferences.getInstance();
  final list=p.getStringList('history')??[];
  if(!context.mounted)return;
  showModalBottomSheet(context:context,isScrollControlled:true,builder:(_)=>SizedBox(
    height:MediaQuery.of(context).size.height*.75,
    child:ListView(padding:const EdgeInsets.all(16),children:[
      const Text('سجل الحسابات',style:TextStyle(fontSize:22,fontWeight:FontWeight.bold)),
      const SizedBox(height:12),
      if(list.isEmpty)const Text('لا توجد حسابات محفوظة بعد.'),
      ...list.map((s){final m=jsonDecode(s);return Card(child:ListTile(title:Text(m['title']),subtitle:Text(m['result']),isThreeLine:true));}),
    ]),
  ));
}

class AboutPage extends StatelessWidget {
  const AboutPage({super.key});
  @override Widget build(BuildContext context)=>ListView(padding:const EdgeInsets.all(20),children:[
    const SizedBox(height:30),
    const CircleAvatar(radius:48,child:Icon(Icons.architecture,size:50)),
    const SizedBox(height:16),
    const Center(child:Text('مساح ومقاول',style:TextStyle(fontSize:28,fontWeight:FontWeight.bold))),
    const Center(child:Text('منصة المساحة والحصر الرقمية')),
    const SizedBox(height:30),
    Card(child:const ListTile(leading:Icon(Icons.person),title:Text('السيرة الذاتية'),subtitle:Text('المهندس / عمرو جمال عوض — مطور منصة مساح ومقاول وأدوات المساحة والحصر الرقمية'))),
    Card(child:const ListTile(leading:Icon(Icons.language),title:Text('الموقع'),subtitle:Text('amrtools.pro'))),
    Card(child:const ListTile(leading:Icon(Icons.verified),title:Text('إصدار التطبيق'),subtitle:Text('1.0.0'))),
    const SizedBox(height:20),
    const Text('التطبيق يقدم أدوات حساب هندسية ومساحية سريعة. النتائج الحسابية تعتمد على البيانات التي يدخلها المستخدم، ويجب مراجعتها هندسياً قبل اعتمادها في التنفيذ.',textAlign:TextAlign.center),
  ]);
}
